package sender

import (
	"fmt"
	"bytes"
	"context"
	"encoding/json"
	"net/http"
	"sync"
	"time"

	"github.com/cenkalti/backoff/v4"
	"github.com/rs/zerolog/log"

	"agent/internal/config"
	"agent/internal/processor"
)

type HTTPSender struct {
	client *http.Client
	url	string
	apiKey string
	hostname string
	batchSize int
	flushInterval time.Duration
	eventChan chan processor.Event
	wg *sync.WaitGroup
}

func NewHTTPSender(cfg *config.Config, wg *sync.WaitGroup) *HTTPSender {
	return &HTTPSender{
		client: &http.Client{Timeout: 15 * time.Second},
		url: cfg.Server.URL,
		apiKey: cfg.Server.APIKey,
		hostname: processor.GetHostname(),
		batchSize: cfg.Agent.BatchSize,
		flushInterval: time.Duration(cfg.Agent.FlushInterval) * time.Millisecond,
		eventChan: make(chan processor.Event, cfg.Agent.BatchSize*5),
		wg: wg,
	}
}

func (s *HTTPSender) Send(event processor.Event) {
	s.eventChan <- event
}

func (s *HTTPSender) Start(ctx context.Context) {
	s.wg.Add(1)
	defer s.wg.Done()

	var batch []processor.Event
	flushTicker := time.NewTicker(s.flushInterval)
	defer flushTicker.Stop()

	heartbeatTicker := time.NewTicker(30 * time.Second)
	defer heartbeatTicker.Stop()

	for {
		select {
		case <- ctx.Done():
			if len(batch) > 0 {
				s.sendBatch(context.Background(), batch)
			}

			return
		case event := <- s.eventChan:
			batch = append(batch, event)
			if len(batch) >= s.batchSize {
				s.sendBatch(ctx, batch)
				batch = nil
			}

		case <- flushTicker.C:
			if len(batch) > 0 {
				s.sendBatch(ctx, batch)
				batch = nil
			}

		case <- heartbeatTicker.C:
			s.sendHeartbeat(ctx)
		}
	}
}

func (s *HTTPSender) sendHeartbeat(ctx context.Context) {
	req, err := http.NewRequestWithContext(ctx, http.MethodPost, s.url+"/heartbeat?hostname="+s.hostname, nil)
	if err != nil {
		return
	}

	req.Header.Set("X-API-Key", s.apiKey)
	resp, err := s.client.Do(req)
	if err != nil {
		return
	}

	resp.Body.Close()
}

func (s *HTTPSender) sendBatch(ctx context.Context, batch []processor.Event) {
	payload, err := json.Marshal(batch)
	if err != nil {
		log.Error().Err(err).Msg("Failed to marshal events")
		return
	}

	op := func() error {
		req, err := http.NewRequestWithContext(ctx, http.MethodPost, s.url, bytes.NewBuffer(payload))
		if err != nil {
			return err
		}

		req.Header.Set("Content-Type", "application/json")
		req.Header.Set("X-API-Key", s.apiKey)

		resp, err := s.client.Do(req)
		if err != nil {
			return err
		}
		defer resp.Body.Close()

		if resp.StatusCode >= 500 {
			return fmt.Errorf("server error: %s", resp.Status)
		}
		if resp.StatusCode >= 400 {
			log.Error().Int("status", resp.StatusCode).Msg("Failed to send events")
			return nil
		}

		return nil
	}

	bo := backoff.NewExponentialBackOff()
	bo.MaxElapsedTime = 5 * time.Minute

	err = backoff.RetryNotify(op, backoff.WithContext(bo, ctx), func(err error, d time.Duration) {
		log.Warn().Err(err).Dur("retry_in", d).Msg("Failed to send events, retrying")
	})

	if err != nil {
		log.Error().Err(err).Msg("Failed to send events after retries")
	}
}
