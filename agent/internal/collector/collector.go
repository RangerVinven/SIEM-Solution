package collector

import (
	"context"

	"siem-agent/internal/processor"
)

// Sender defines the interface for sending events
type Sender interface {
	Send(event processor.ECSEvent)
}

// Collector defines the interface for log collectors
type Collector interface {
	Start(ctx context.Context, sender Sender) error
}
