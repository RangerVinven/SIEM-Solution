import time
from demo_utils import get_api_key, send_events

INFECTED_HOST = "LIB-PC-01"

def main():
    api_key = get_api_key()
    print(f"Simulating ransomware attack on {INFECTED_HOST}...")

    for i in range(1, 13):
        event = {
            "event": {
                "dataset": "windows.event",
                "category": "file_tampering",
                "action": "file_modified",
            },
            "host": {"hostname": INFECTED_HOST},
            "message": f"File encrypted: document_{i:03d}.docx -> document_{i:03d}.docx.locked",
        }
        send_events([event], api_key)
        print(f"  Sent file tampering event {i}/12")
        time.sleep(0.3)

    print("Done. Check the Alerts page for a Ransomware alert.")

if __name__ == "__main__":
    main()
