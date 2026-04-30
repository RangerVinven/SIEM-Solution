import time
from demo_utils import get_api_key, send_events

ATTACKER_HOST = "ROOM204-PC-02"

def main():
    api_key = get_api_key()
    print(f"Simulating brute force attack from {ATTACKER_HOST}...")

    for i in range(1, 8):
        event = {
            "event": {
                "dataset": "windows.event",
                "category": "authentication",
                "outcome": "failure",
                "action": "user_login",
            },
            "host": {"hostname": ATTACKER_HOST},
            "log": {"level": "warn"},
            "message": f"Logon failure: unknown username or bad password (attempt {i})",
        }
        send_events([event], api_key)
        print(f"  Sent failed login attempt {i}/7")
        time.sleep(0.5)

    print("Done. Check the Alerts page for a Brute Force alert.")

if __name__ == "__main__":
    main()
