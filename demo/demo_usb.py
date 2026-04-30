from demo_utils import get_api_key, send_events

USB_HOST = "STAFFROOM-PC-01"

def main():
    api_key = get_api_key()
    print(f"Simulating USB insertion on {USB_HOST}...")

    event = {
        "event": {
            "dataset": "windows.event",
            "category": "host",
            "action": "removable_media_inserted",
        },
        "host": {"hostname": USB_HOST},
        "message": "Removable storage device connected: USB Mass Storage Device (16GB)",
    }
    send_events([event], api_key)
    print("Done. Check the Alerts page for a USB Device Inserted alert.")

if __name__ == "__main__":
    main()
