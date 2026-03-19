package config

import (
	"fmt"
	"os"

	"gopkg.in/yaml.v3"
)

type Config struct {
	Server struct {
		URL    string `yaml:"url"`
		APIKey string `yaml:"api_key"`
	} `yaml:"server"`
	Agent struct {
		BatchSize     int `yaml:"batch_size"`
		FlushInterval int `yaml:"flush_interval_ms"`
	} `yaml:"agent"`
	Collectors struct {
		WindowsEvents []string `yaml:"windows_events,omitempty"`
		Files         []string `yaml:"files,omitempty"`
	} `yaml:"collectors"`
}

func LoadConfig(path string) (*Config, error) {
	data, err := os.ReadFile(path)
	if err != nil {
		return nil, fmt.Errorf("failed to read config file: %w", err)
	}

	var cfg Config
	if err := yaml.Unmarshal(data, &cfg); err != nil {
		return nil, fmt.Errorf("failed to parse config file: %w", err)
	}

	// Set defaults
	if cfg.Agent.BatchSize == 0 {
		cfg.Agent.BatchSize = 100
	}
	if cfg.Agent.FlushInterval == 0 {
		cfg.Agent.FlushInterval = 5000 // 5 seconds
	}

	return &cfg, nil
}
