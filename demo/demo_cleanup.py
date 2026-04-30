import psycopg2
import requests

def clear_alerts():
    conn = psycopg2.connect(host="localhost", port=5003, dbname="alert_db", user="siem_user", password="siem_password")
    cur = conn.cursor()
    cur.execute("DELETE FROM alert_remediation_steps")
    cur.execute("DELETE FROM alerts")
    conn.commit()
    cur.close()
    conn.close()
    print("  Cleared alerts")

def clear_agents():
    conn = psycopg2.connect(host="localhost", port=5004, dbname="agent_db", user="siem_user", password="siem_password")
    cur = conn.cursor()
    cur.execute("DELETE FROM agents")
    conn.commit()
    cur.close()
    conn.close()
    print("  Cleared agents")

def clear_rules():
    conn = psycopg2.connect(host="localhost", port=5002, dbname="analysis_db", user="siem_user", password="siem_password")
    cur = conn.cursor()
    cur.execute("DELETE FROM remediation_steps")
    cur.execute("DELETE FROM rules")
    conn.commit()
    cur.close()
    conn.close()
    print("  Cleared rules")

def clear_schools():
    conn = psycopg2.connect(host="localhost", port=5001, dbname="accountdb", user="postgres", password="password")
    cur = conn.cursor()
    cur.execute("DELETE FROM users")
    cur.execute("DELETE FROM locations")
    cur.execute("DELETE FROM schools")
    conn.commit()
    cur.close()
    conn.close()
    print("  Cleared schools and users")

def clear_elasticsearch():
    res = requests.delete("http://localhost:9200/logs")
    if res.status_code in (200, 404):
        print("Cleared Elasticsearch logs")
    else:
        print(f"Warning: Elasticsearch returned {res.status_code}")

def clear_redis():
    import redis
    r = redis.Redis(host="localhost", port=6379)
    keys = r.keys("counter:*")
    if keys:
        r.delete(*keys)
    r.flushdb()
    print("Cleared Redis counters and cache")

def main():
    print("Cleaning up demo environment...")
    clear_alerts()
    clear_agents()
    clear_rules()
    clear_schools()
    clear_elasticsearch()
    clear_redis()
    print("\nCleanup done!")

if __name__ == "__main__":
    main()
