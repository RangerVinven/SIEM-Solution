package collector

import "agent/internal/processor"

type Sender interface {
	Send(event processor.Event)
}
