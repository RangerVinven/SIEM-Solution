//go:build windows
// +build windows

package collector

import (
	"bufio"
	"context"
	"fmt"
	"os/exec"
	"sync"
	"time"

	"github.com/rs/zerolog/log"

	"siem-agent/internal/config"
	"siem-agent/internal/processor"
)

type WindowsEventCollector struct {
	channels []string
	wg       *sync.WaitGroup
}

func NewWindowsEventCollector(cfg *config.Config, wg *sync.WaitGroup) *WindowsEventCollector {
	return &WindowsEventCollector{
		channels: cfg.Collectors.WindowsEvents,
		wg:       wg,
	}
}

func (c *WindowsEventCollector) Start(ctx context.Context, sender Sender) error {
	if len(c.channels) == 0 {
		log.Warn().Msg("No Windows Event channels configured")
		return nil
	}

	for _, channel := range c.channels {
		c.wg.Add(1)
		go c.tailEventLog(ctx, channel, sender)
	}

	return nil
}

func (c *WindowsEventCollector) tailEventLog(ctx context.Context, channel string, sender Sender) {
	defer c.wg.Done()
	log.Info().Str("channel", channel).Msg("Started polling Windows Event Log")

	// In a fully native high-throughput production environment, this should be replaced
	// with direct calls to `wevtapi.dll` (EvtSubscribe). We use an optimized PowerShell
	// polling loop here as a robust cross-compiled fallback.
	
	ticker := time.NewTicker(5 * time.Second)
	defer ticker.Stop()

	lastTime := time.Now().UTC().Format(time.RFC3339)

	for {
		select {
		case <-ctx.Done():
			log.Debug().Str("channel", channel).Msg("Stopping Windows Event collection due to context cancellation")
			return
		case <-ticker.C:
			script := fmt.Sprintf(`
				$events = Get-WinEvent -FilterHashtable @{LogName='%s'; StartTime=[datetime]::Parse('%s')} -ErrorAction SilentlyContinue
				if ($events) {
					$events | ConvertTo-Json -Compress
				}
			`, channel, lastTime)

			lastTime = time.Now().UTC().Format(time.RFC3339)

			cmd := exec.CommandContext(ctx, "powershell", "-NoProfile", "-NonInteractive", "-Command", script)
			
			stdout, err := cmd.StdoutPipe()
			if err != nil {
				log.Error().Err(err).Str("channel", channel).Msg("Failed to create stdout pipe for PowerShell")
				continue
			}

			if err := cmd.Start(); err != nil {
				log.Error().Err(err).Str("channel", channel).Msg("Failed to start PowerShell command")
				continue
			}

			scanner := bufio.NewScanner(stdout)
			for scanner.Scan() {
				text := scanner.Text()
				if text == "" || text == "null" {
					continue
				}

				event := processor.NewECSEvent(text)
				event.Event.Dataset = "windows.event"
				event.Event.Provider = channel
				event.Event.Category = "host"
				
				sender.Send(event)
			}

			// Ignore errors from wait, as Get-WinEvent returns an error if no events are found
			_ = cmd.Wait()
		}
	}
}

// Provide a stub for FileTailCollector so that compilation doesn't fail on Windows
type FileTailCollector struct{}

func NewFileTailCollector(cfg *config.Config, wg *sync.WaitGroup) *FileTailCollector {
	return nil
}

func (c *FileTailCollector) Start(ctx context.Context, sender Sender) error {
	return fmt.Errorf("File tailing not supported in this stub on Windows")
}
