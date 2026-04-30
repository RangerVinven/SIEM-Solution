// GOOS=windows GOARCH=amd64 go build ./cmd/agent
package main

import (
	"context"
	"flag"
	"os"
	"os/signal"
	"sync"
	"syscall"

	"github.com/rs/zerolog"
	"github.com/rs/zerolog/log"

	"agent/internal/collector"
	"agent/internal/config"
	"agent/internal/sender"
)

func main() {
	log.Logger = log.Output(zerolog.ConsoleWriter{Out: os.Stdout, TimeFormat: "2006-01-02T15:04:05Z07:00"})

	configPath := flag.String("config", "config.yaml", "Path to config file")
	debug := flag.Bool("debug", false, "Enable debug logging")
	flag.Parse()

	if *debug {
		zerolog.SetGlobalLevel(zerolog.DebugLevel)
	} else {
		zerolog.SetGlobalLevel(zerolog.InfoLevel)
	}

	cfg, err := config.LoadConfig(*configPath)
	if err != nil {
		log.Fatal().Err(err).Msg("Failed to load config")
	}

	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	var wg sync.WaitGroup

	// Starts the sender
	httpSender := sender.NewHTTPSender(cfg, &wg)
	go httpSender.Start(ctx)

	// Starts collecting the events every 5 seconds, or whenever the batch fills up (whichever happens first)
	winCollector := collector.NewWindowsCollector(cfg, &wg)
	if err := winCollector.Start(ctx, httpSender); err != nil {
		log.Fatal().Err(err).Msg("Failed to start Windows collector")
	}

	sigChan := make(chan os.Signal, 1)
	signal.Notify(sigChan, syscall.SIGINT, syscall.SIGTERM)
	<-sigChan

	log.Info().Msg("Shutting down agent")
	cancel()
	wg.Wait()
	log.Info().Msg("Agent stopped.")
}
