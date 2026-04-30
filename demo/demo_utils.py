import psycopg2
import requests

AGGREGATE_URL = "http://localhost:8089/aggregate"

HOSTNAMES = ["LIB-PC-01", "ROOM204-PC-02", "ROOM204-PC-03", "STAFFROOM-PC-01", "HALL-PC-01"]

def get_api_key():
    conn = psycopg2.connect(host="localhost", port=5001, dbname="accountdb", user="postgres", password="password")
    cur = conn.cursor()
    cur.execute("SELECT api_key FROM schools LIMIT 1")
    row = cur.fetchone()
    cur.close()
    conn.close()
    if not row:
        raise RuntimeError("No school found in the database. Have you signed up and created a school?")
    return row[0]

def send_events(events, api_key):
    res = requests.post(AGGREGATE_URL, json=events, headers={"X-API-Key": api_key})
    res.raise_for_status()
