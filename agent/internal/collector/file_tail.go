//go:build linux || darwin
// +build linux darwin

package collector

import (
	"context"
	"fmt"
	"sync"

	"github.com/nxadm/tail"
	"github.com/rs/zerolog/log"

	"siem-agent/internal/config"
	"siem-agent/internal/processor"
)

type FileTailCollector struct {
	files []string
	wg    *sync.WaitGroup
}

func NewFileTailCollector(cfg *config.Config, wg *sync.WaitGroup) *FileTailCollector {
	return &FileTailCollector{
		files: cfg.Collectors.Files,
		wg:    wg,
	}
}

func (c *FileTailCollector) Start(ctx context.Context, sender Sender) error {
	if len(c.files) == 0 {
		log.Warn().Msg("No files configured to tail")
		return nil
	}

	for _, file := range c.files {
		c.wg.Add(1)
		go func(filePath string) {
			defer c.wg.Done()

			t, err := tail.TailFile(filePath, tail.Config{
				Follow: true,
				ReOpen: true,
				Poll:   true,
				Location: &tail.SeekInfo{Offset: 0, Whence: 2}, // End of file
			})
			if err != nil {
				log.Error().Err(err).Str("file", filePath).Msg("Error tailing file")
				return
			}
			defer t.Stop()

			log.Info().Str("file", filePath).Msg("Started tailing log file")

			for {
				select {
				case <-ctx.Done():
					log.Debug().Str("file", filePath).Msg("Stopping tail on file due to context cancellation")
					return
				case line, ok := <-t.Lines:
					if !ok {
						return
					}
					if line.Err != nil {
						log.Error().Err(line.Err).Str("file", filePath).Msg("Error reading line")
						continue
					}

					event := processor.NewECSEvent(line.Text)
					event.Log.FilePath = filePath
					event.Event.Dataset = "file_tail"
					event.Event.Category = "host"
					event.Event.Type = "info"
					
					sender.Send(event)
				}
			}
		}(file)
	}

	return nil
}

// Provide a stub for WindowsEventCollector so that compilation doesn't fail on Linux/Mac
type WindowsEventCollector struct{}

func NewWindowsEventCollector(cfg *config.Config, wg *sync.WaitGroup) *WindowsEventCollector {
	return nil
}

func (c *WindowsEventCollector) Start(ctx context.Context, sender Sender) error {
	return fmt.Errorf("Windows Event Collection is not supported on this platform")
}
