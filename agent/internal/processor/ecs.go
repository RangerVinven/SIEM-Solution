package processor

import (
	"time"

	"github.com/google/uuid"
	"github.com/shirou/gopsutil/v3/host"
)

// ECSEvent represents the Elastic Common Schema (ECS) base format
type ECSEvent struct {
	Timestamp string            `json:"@timestamp"`
	Event     Event             `json:"event"`
	Host      Host              `json:"host"`
	Message   string            `json:"message"`
	Log       Log               `json:"log,omitempty"`
	Tags      []string          `json:"tags,omitempty"`
	Labels    map[string]string `json:"labels,omitempty"`
}

type Event struct {
	ID       string `json:"id,omitempty"`
	Kind     string `json:"kind,omitempty"`
	Category string `json:"category,omitempty"`
	Type     string `json:"type,omitempty"`
	Dataset  string `json:"dataset,omitempty"`
	Provider string `json:"provider,omitempty"`
	Action   string `json:"action,omitempty"`
	Outcome  string `json:"outcome,omitempty"`
	Original string `json:"original,omitempty"`
}

type Host struct {
	Hostname     string `json:"hostname,omitempty"`
	ID           string `json:"id,omitempty"`
	Architecture string `json:"architecture,omitempty"`
	OS           OS     `json:"os,omitempty"`
}

type OS struct {
	Name     string `json:"name,omitempty"`
	Platform string `json:"platform,omitempty"`
	Version  string `json:"version,omitempty"`
	Family   string `json:"family,omitempty"`
}

type Log struct {
	FilePath string `json:"file.path,omitempty"`
	Level    string `json:"level,omitempty"`
}

var (
	hostInfo *host.InfoStat
)

func init() {
	var err error
	// Fetch rich host metadata using gopsutil
	hostInfo, err = host.Info()
	if err != nil {
		// Fallback to basic info if gopsutil fails
		hostInfo = &host.InfoStat{
			Hostname: "unknown",
			OS:       "unknown",
		}
	}
}

// NewECSEvent creates a new base ECSEvent populated with standard host fields
func NewECSEvent(message string) ECSEvent {
	return ECSEvent{
		Timestamp: time.Now().UTC().Format(time.RFC3339Nano),
		Event: Event{
			ID:       uuid.New().String(),
			Original: message,
			Kind:     "event",
		},
		Host: Host{
			Hostname:     hostInfo.Hostname,
			ID:           hostInfo.HostID,
			Architecture: hostInfo.KernelArch,
			OS: OS{
				Name:     hostInfo.OS,
				Platform: hostInfo.Platform,
				Version:  hostInfo.PlatformVersion,
				Family:   hostInfo.PlatformFamily,
			},
		},
		Message: message,
	}
}

func GetHostname() string {
	return hostInfo.Hostname
}
