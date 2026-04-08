// FULLY CREATED BY AI. ALL THE CODE IN THIS AGENT/ DIRECTORY WAS WRITTEN BY AN AI, NOT BY ME.
package main

import (
	"context"
	"flag"
	"os"
	"os/signal"
	"runtime"
	"sync"
	"syscall"

	"github.com/rs/zerolog"
	"github.com/rs/zerolog/log"

	"siem-agent/internal/collector"
	"siem-agent/internal/config"
	"siem-agent/internal/sender"
)

func main() {
	zerolog.TimeFieldFormat = zerolog.TimeFormatUnix
	log.Logger = log.Output(zerolog.ConsoleWriter{Out: os.Stdout, TimeFormat: "2006-01-02T15:04:05.999Z07:00"})

	configPath := flag.String("config", "config.yaml", "Path to configuration file")
	debug := flag.Bool("debug", false, "Enable debug logging")
	flag.Parse()

	if *debug {
		zerolog.SetGlobalLevel(zerolog.DebugLevel)
	} else {
		zerolog.SetGlobalLevel(zerolog.InfoLevel)
	}

	log.Info().Str("os", runtime.GOOS).Str("arch", runtime.GOARCH).Msg("Starting SIEM Agent")

	cfg, err := config.LoadConfig(*configPath)
	if err != nil {
		log.Fatal().Err(err).Msg("Error loading configuration")
	}

	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	var wg sync.WaitGroup

	httpSender := sender.NewHTTPSender(cfg, &wg)
	go httpSender.Start(ctx)

	if runtime.GOOS == "windows" {
		winCollector := collector.NewWindowsEventCollector(cfg, &wg)
		if winCollector != nil {
			if err := winCollector.Start(ctx, httpSender); err != nil {
				log.Error().Err(err).Msg("Failed to start Windows Event Collector")
			}
		}
	} else {
		fileCollector := collector.NewFileTailCollector(cfg, &wg)
		if fileCollector != nil {
			if err := fileCollector.Start(ctx, httpSender); err != nil {
				log.Error().Err(err).Msg("Failed to start File Tail Collector")
			}
		}
	}

	sigChan := make(chan os.Signal, 1)
	signal.Notify(sigChan, syscall.SIGINT, syscall.SIGTERM)

	<-sigChan
	log.Info().Msg("Received shutdown signal, initiating graceful shutdown...")
	cancel()

	wg.Wait()
	log.Info().Msg("Agent stopped successfully.")
}
