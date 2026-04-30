package collector

import (
	"bufio"
	"context"
	"fmt"
	"os/exec"
	"sync"
	"time"

	"github.com/rs/zerolog/log"

	"agent/internal/config"
	"agent/internal/processor"
)

type WindowsCollector struct {
	channels []string
	wg       *sync.WaitGroup
}

func NewWindowsCollector(cfg *config.Config, wg *sync.WaitGroup) *WindowsCollector {
	return &WindowsCollector{
		channels: cfg.Collectors.WindowsEvents,
		wg:       wg,
	}
}

func (c *WindowsCollector) Start(ctx context.Context, sender Sender) error {
	if len(c.channels) == 0 {
		log.Warn().Msg("No Windows Event channels configured")
		return nil
	}

	for _, channel := range c.channels {
		c.wg.Add(1)
		go c.pollChannel(ctx, channel, sender)
	}

	return nil
}

func (c *WindowsCollector) pollChannel(ctx context.Context, channel string, sender Sender) {
	defer c.wg.Done()
	log.Info().Str("channel", channel).Msg("Started polling Windows Event Log")

	ticker := time.NewTicker(5 * time.Second)
	defer ticker.Stop()

	lastTime := time.Now().UTC().Format(time.RFC3339)

	for {
		select {
		case <-ctx.Done():
			return
		case <-ticker.C:
			script := fmt.Sprintf(`
				$events = @(Get-WinEvent -FilterHashtable @{LogName='%s'; StartTime=[datetime]::Parse('%s')} -ErrorAction SilentlyContinue)
				if ($events.Count -gt 0) { $events | ConvertTo-Json -Compress }
			`, channel, lastTime)

			lastTime = time.Now().UTC().Format(time.RFC3339)

			cmd := exec.CommandContext(ctx, "powershell", "-NoProfile", "-NonInteractive", "-Command", script)

			stdout, err := cmd.StdoutPipe()
			if err != nil {
				log.Error().Err(err).Str("channel", channel).Msg("Failed to get stdout pipe")
				continue
			}

			if err := cmd.Start(); err != nil {
				log.Error().Err(err).Str("channel", channel).Msg("Failed to start PowerShell")
				continue
			}

			scanner := bufio.NewScanner(stdout)
			for scanner.Scan() {
				line := scanner.Text()
				if line == "" || line == "null" {
					continue
				}

				event := processor.NewEvent(line)
				event.EventData.Dataset = "windows.event"
				event.EventData.Provider = channel
				event.EventData.Category = "host"

				sender.Send(event)
			}

			_ = cmd.Wait()
		}
	}
}
