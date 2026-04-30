import time
import random
import itertools
from demo_utils import get_api_key, send_events, HOSTNAMES

NORMAL_EVENTS = [
    {"event": {"dataset": "windows.event", "category": "authentication", "outcome": "success", "action": "user_login"},  "message": "User logged in successfully"},
    {"event": {"dataset": "windows.event", "category": "file",           "outcome": "success", "action": "file_read"},   "message": "File read access granted"},
    {"event": {"dataset": "windows.event", "category": "network",        "outcome": "success", "action": "connection"},  "message": "Outbound network connection established"},
    {"event": {"dataset": "windows.event", "category": "process",        "outcome": "success", "action": "process_start"}, "message": "Process started"},
    {"event": {"dataset": "windows.event", "category": "authentication", "outcome": "success", "action": "user_logout"}, "message": "User logged out"},
    {"event": {"dataset": "windows.event", "category": "file",           "outcome": "success", "action": "file_write"},  "message": "File saved"},
    {"event": {"dataset": "windows.event", "category": "network",        "outcome": "success", "action": "dns_lookup"},  "message": "DNS lookup performed"},
    {"event": {"dataset": "windows.event", "category": "system",         "outcome": "success", "action": "service_start"}, "message": "Windows service started"},
]

def main():
    api_key = get_api_key()
    print(f"Sending normal activity (Ctrl+C to stop)...")

    hostname_cycle = itertools.cycle(HOSTNAMES)

    while True:
        hostname = next(hostname_cycle)
        template = random.choice(NORMAL_EVENTS)

        event = {**template, "host": {"hostname": hostname}}
        send_events([event], api_key)

        print(f"[{hostname}] {template['message']}")
        time.sleep(random.uniform(1.5, 4.0))

if __name__ == "__main__":
    main()
