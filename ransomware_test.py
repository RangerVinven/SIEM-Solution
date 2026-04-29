import requests
import json

URL = "http://localhost:8089/aggregate"
API_KEY = "d9a5163c-540b-48db-963d-6f2c082f989a"

def simulate_ransomware_logs():
    logs = []
    for i in range(10):
        logs.append({
            "event": {
                "dataset": "windows.event",
                "original": json.dumps({"Id": 4663, "Message":
                                        f"Modified file_{i}.docx"})
                },
            "host": {"hostname": "OFFICE-PC-01"},
            "message": "File system access"
            })

    headers = {"X-API-Key": API_KEY}
    requests.post(URL, json=logs, headers=headers)

simulate_ransomware_logs()


