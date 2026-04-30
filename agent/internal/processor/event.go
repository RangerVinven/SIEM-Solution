package processor

import (
	"time"
	"github.com/google/uuid"
	"github.com/shirou/gopsutil/v3/host"
)

type Event struct {
	Timestamp string `json:"@timestamp"`
	EventData EventData `json:"event"`
	Host Host `json:"host"`
	Message string `json:"message"`
	Log Log `json:"log,omitempty"`
	Tags []string `json:"tags,omitempty"`
	Labels map[string]string `json:"labels,omitempty"`
}

type EventData struct {
	ID string `json:"id,omitempty"`
	Kind string `json:"kind,omitempty"`
	Category string `json:"category,omitempty"`
	Type string `json:"type,omitempty"`
	Dataset string `json:"dataset,omitempty"`
	Provider string `json:"provider,omitempty"`
	Original string `json:"original,omitempty"`
}

type Host struct {
	Hostname string `json:"hostname,omitempty"`
	ID string `json:"id,omitempty"`
	Architecture string `json:"architecture,omitempty"`
	OS OS `json:"os,omitempty"`
}

type OS struct {
	Name string `json:"name,omitempty"`
	Platform string `json:"platform,omitempty"`
	Version string `json:"version,omitempty"`
	Family string `json:"family,omitempty"`
}

type Log struct {
	Level string `json:"level,omitempty"`
}

var hostInfo *host.InfoStat

func init() {
	var err error
	hostInfo, err = host.Info()
	if err != nil {
		hostInfo = &host.InfoStat{Hostname: "unknown"}
	}
}

func NewEvent(message string) Event {
	return Event{
		Timestamp: time.Now().UTC().Format(time.RFC3339Nano),
		Message: message,
		EventData: EventData{
			ID: uuid.New().String(),
			Kind: "event",
			Original: message,
		},
		Host: Host{
			Hostname: hostInfo.Hostname,
			ID: hostInfo.HostID,
			Architecture: hostInfo.KernelArch,
			OS: OS{
				Name: hostInfo.OS,
				Platform: hostInfo.Platform,
				Version: hostInfo.PlatformVersion,
				Family: hostInfo.PlatformFamily,
			},
		},
	}
}

func GetHostname() string {
	return hostInfo.Hostname
}
