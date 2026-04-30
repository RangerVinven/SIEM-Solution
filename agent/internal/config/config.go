package config

import (
	"fmt"
	"os"
	"gopkg.in/yaml.v3"
)

type Config struct {
	Server struct {
		URL string `yaml:"url"`
		APIKey string `yaml:"api_key"`
	} `yaml:"server"`
	Agent struct {
		BatchSize int `yaml:"batch_size"`
		FlushInterval int `yaml:"flush_interval_ms"`
	} `yaml:"agent"`
	Collectors struct {
		WindowsEvents []string `yaml:"windows_events"`
	} `yaml:"collectors"`
}

// Loads the config from the yaml config file
func LoadConfig(path string) (*Config, error) {
	data, err := os.ReadFile(path)
	if err != nil {
		return nil, fmt.Errorf("Couldn't read config file: %w", err)
	}

	var cfg Config
	if err := yaml.Unmarshal(data, &cfg); err != nil {
		return nil, fmt.Errorf("Couldn't parse config file: %w", err)
	}

	if cfg.Agent.BatchSize == 0 {
		cfg.Agent.BatchSize = 100
	}

	if cfg.Agent.FlushInterval == 0 {
		cfg.Agent.FlushInterval = 5000
	}

	return &cfg, nil
}
