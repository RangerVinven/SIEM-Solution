package sender

import (
	"bytes"
	"context"
	"encoding/json"
	"net/http"
	"sync"
	"time"

	"github.com/cenkalti/backoff/v4"
	"github.com/rs/zerolog/log"

	"siem-agent/internal/config"
	"siem-agent/internal/processor"
)

type HTTPSender struct {
	client        *http.Client
	url           string
	apiKey        string
	hostname      string
	batchSize     int
	flushInterval time.Duration
	eventChan     chan processor.ECSEvent
	wg            *sync.WaitGroup
}

func NewHTTPSender(cfg *config.Config, wg *sync.WaitGroup) *HTTPSender {
	transport := &http.Transport{
		MaxIdleConns:        100,
		MaxIdleConnsPerHost: 100,
		IdleConnTimeout:     90 * time.Second,
	}

	return &HTTPSender{
		client:        &http.Client{Timeout: 15 * time.Second, Transport: transport},
		url:           cfg.Server.URL,
		apiKey:        cfg.Server.APIKey,
		hostname:      processor.GetHostname(),
		batchSize:     cfg.Agent.BatchSize,
		flushInterval: time.Duration(cfg.Agent.FlushInterval) * time.Millisecond,
		eventChan:     make(chan processor.ECSEvent, cfg.Agent.BatchSize*5),
		wg:            wg,
	}
}

func (s *HTTPSender) Send(event processor.ECSEvent) {
	s.eventChan <- event
}

func (s *HTTPSender) Start(ctx context.Context) {
	s.wg.Add(1)
	defer s.wg.Done()

	var batch []processor.ECSEvent
	ticker := time.NewTicker(s.flushInterval)
	defer ticker.Stop()

	heartbeatTicker := time.NewTicker(30 * time.Second)
	defer heartbeatTicker.Stop()

	for {
		select {
		case <-ctx.Done():
			log.Info().Msg("HTTP Sender shutting down, flushing remaining events...")
			if len(batch) > 0 {
				s.sendBatchWithRetry(context.Background(), batch)
			}
			return
		case event := <-s.eventChan:
			batch = append(batch, event)
			if len(batch) >= s.batchSize {
				s.sendBatchWithRetry(ctx, batch)
				batch = make([]processor.ECSEvent, 0, s.batchSize)
			}
		case <-ticker.C:
			if len(batch) > 0 {
				s.sendBatchWithRetry(ctx, batch)
				batch = make([]processor.ECSEvent, 0, s.batchSize)
			}
		case <-heartbeatTicker.C:
			s.sendHeartbeat(ctx)
		}
	}
}

func (s *HTTPSender) sendHeartbeat(ctx context.Context) {
	url := s.url + "/heartbeat?hostname=" + s.hostname
	req, _ := http.NewRequestWithContext(ctx, http.MethodPost, url, nil)
	req.Header.Set("X-API-Key", s.apiKey)

	resp, err := s.client.Do(req)
	if err != nil {
		log.Debug().Err(err).Msg("Failed to send heartbeat")
		return
	}
	defer resp.Body.Close()
}

func (s *HTTPSender) sendBatchWithRetry(ctx context.Context, batch []processor.ECSEvent) {
	payload, err := json.Marshal(batch)
	if err != nil {
		log.Error().Err(err).Msg("Failed to marshal event batch")
		return
	}

	operation := func() error {
		req, err := http.NewRequestWithContext(ctx, http.MethodPost, s.url, bytes.NewBuffer(payload))
		if err != nil {
			return err
		}

		req.Header.Set("Content-Type", "application/json")
		req.Header.Set("X-API-Key", s.apiKey)

		resp, err := s.client.Do(req)
		if err != nil {
			log.Warn().Err(err).Msg("Network error sending logs, will retry")
			return err
		}
		defer resp.Body.Close()

		if resp.StatusCode >= 500 {
			log.Warn().Int("status", resp.StatusCode).Msg("SIEM server error, will retry")
			return err
		}

		if resp.StatusCode >= 400 && resp.StatusCode < 500 {
			log.Error().Int("status", resp.StatusCode).Msg("SIEM rejected logs (4xx), dropping batch")
			return nil
		}

		return nil
	}

	bo := backoff.NewExponentialBackOff()
	bo.MaxElapsedTime = 5 * time.Minute

	err = backoff.RetryNotify(operation, backoff.WithContext(bo, ctx), func(err error, t time.Duration) {
		log.Debug().Err(err).Dur("retry_in", t).Msg("Retrying batch transmission")
	})

	if err != nil {
		log.Error().Err(err).Msg("Failed to send batch after multiple retries, dropping events")
	} else {
		log.Debug().Int("count", len(batch)).Msg("Successfully sent event batch to SIEM")
	}
}
