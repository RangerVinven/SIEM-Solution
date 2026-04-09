#!/bin/bash
# This file was made entirely by AI. Soely for the purposes of seeding the databases

# Configuration
ACCOUNT_URL="http://localhost:8081"
AGENT_URL="http://localhost:8086"
AGGREGATOR_URL="http://localhost:8089"
LOG_QUERY_URL="http://localhost:8083"

COOKIE_FILE="cookies.txt"
ADMIN_EMAIL="admin@school.com"
ADMIN_PASS="password123"
SCHOOL_NAME="Westside Academy"

echo "=== Seeding SIEM Solution ==="

# 1. Register Admin User
echo "Registering admin user..."
curl -s -X POST "$ACCOUNT_URL/api/account/users" \
     -H "Content-Type: application/json" \
     -d "{\"firstName\":\"Principal\", \"lastName\":\"Skinner\", \"email\":\"$ADMIN_EMAIL\", \"password\":\"$ADMIN_PASS\"}" > /dev/null

# 2. Login
echo "Logging in..."
curl -s -X POST "$ACCOUNT_URL/api/account/login" \
     -H "Content-Type: application/json" \
     -c "$COOKIE_FILE" \
     -d "{\"email\":\"$ADMIN_EMAIL\", \"password\":\"$ADMIN_PASS\"}" > /dev/null

# 3. Create School
echo "Creating school..."
SCHOOL_DATA=$(curl -s -X POST "$ACCOUNT_URL/api/account/schools" \
     -H "Content-Type: application/json" \
     -b "$COOKIE_FILE" \
     -d "{\"name\":\"$SCHOOL_NAME\"}")

# Extract schoolId using Python for reliability
SCHOOL_ID=$(echo "$SCHOOL_DATA" | python3 -c "import sys, json; print(json.load(sys.stdin).get('id', ''))")
if [ -z "$SCHOOL_ID" ]; then
    echo "Error: School already exists or failed to create. Trying to fetch existing school..."
    USER_DATA=$(curl -s -X GET "$ACCOUNT_URL/api/account" -b "$COOKIE_FILE")
    SCHOOL_ID=$(echo "$USER_DATA" | python3 -c "import sys, json; print(json.load(sys.stdin).get('schoolId', ''))")
fi

if [ -z "$SCHOOL_ID" ]; then
    echo "Fatal Error: Could not obtain School ID."
    exit 1
fi
echo "Using school: $SCHOOL_NAME (ID: $SCHOOL_ID)"

# 4. Get API Key
echo "Retrieving API Key..."
API_KEY=$(curl -s -X GET "$ACCOUNT_URL/api/account/schools/$SCHOOL_ID/api-key" -b "$COOKIE_FILE")
echo "API Key retrieved."

# 5. Add a technician employee
echo "Adding technician..."
curl -s -X POST "$ACCOUNT_URL/api/account/schools/$SCHOOL_ID/employees" \
     -H "Content-Type: application/json" \
     -b "$COOKIE_FILE" \
     -d "{\"firstName\":\"Willie\", \"lastName\":\"Groundskeeper\", \"email\":\"willie@school.com\", \"password\":\"password123\", \"role\":\"TECHNICAL_ADMIN\"}" > /dev/null

# 6. Generate Agents and Logs by sending raw events
echo "Generating agents and logs..."

# Hostnames for dummy agents
HOSTS=("server-win-01" "server-lin-02" "reception-pc" "lab-pc-15")
LOCATIONS=("Main Server Room" "Backup Server Room" "Reception Desk" "IT Lab")

for i in "${!HOSTS[@]}"; do
    HOSTNAME=${HOSTS[$i]}
    TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
    
    # Send a sample log event
    echo "Sending event for $HOSTNAME..."
    curl -s -X POST "$AGGREGATOR_URL/aggregate" \
         -H "X-API-Key: $API_KEY" \
         -H "Content-Type: application/json" \
         -d "[{
            \"@timestamp\": \"$TIMESTAMP\",
            \"event\": { \"category\": \"authentication\", \"type\": \"start\", \"outcome\": \"success\" },
            \"host\": { \"hostname\": \"$HOSTNAME\" },
            \"message\": \"User logged in successfully\",
            \"log\": { \"level\": \"info\" }
         }]" > /dev/null
done

# 7. Assign agents to their locations
echo "Waiting for agents to register in agent-service..."
MAX_ATTEMPTS=30
ATTEMPT=1
while [ $ATTEMPT -le $MAX_ATTEMPTS ]; do
    AGENT_LIST=$(curl -s -X GET "$AGENT_URL/agents" -b "$COOKIE_FILE")
    COUNT=$(echo "$AGENT_LIST" | python3 -c "import sys, json; data=json.load(sys.stdin); print(len(data))")
    
    if [ "$COUNT" -ge 4 ]; then
        echo "Found $COUNT agents. Proceeding with assignment..."
        break
    fi
    
    echo "Attempt $ATTEMPT/$MAX_ATTEMPTS: Found $COUNT agents. Waiting..."
    sleep 2
    ATTEMPT=$((ATTEMPT + 1))
done

# Parse agent list and assign locations
for i in "${!HOSTS[@]}"; do
    HOSTNAME=${HOSTS[$i]}
    LOCATION=${LOCATIONS[$i]}
    
    # Find agent ID for this hostname using python
    AGENT_ID=$(echo "$AGENT_LIST" | python3 -c "import sys, json; data=json.load(sys.stdin); matches=[a['id'] for a in data if a['hostname'] == '$HOSTNAME']; print(matches[0] if matches else '')")
    
    if [ ! -z "$AGENT_ID" ]; then
        echo "Assigning $HOSTNAME to $LOCATION..."
        curl -s -X PUT "$AGENT_URL/agents/$AGENT_ID/assign?schoolId=$SCHOOL_ID&schoolName=$(echo "$SCHOOL_NAME" | sed 's/ /%20/g')&location=$(echo "$LOCATION" | sed 's/ /%20/g')" \
             -b "$COOKIE_FILE" > /dev/null
    fi
done

# 8. Seed some manual alerts into Postgres
echo "Seeding alerts into database..."
# Find the actual container name for the alert database
ALERT_DB_CONTAINER=$(docker ps --filter name=alert-db --format '{{.Names}}' | head -n 1)

if [ ! -z "$ALERT_DB_CONTAINER" ]; then
    echo "Found alert-db container: $ALERT_DB_CONTAINER"
    
    # High Severity Alert
    docker exec -i "$ALERT_DB_CONTAINER" psql -U siem_user -d alert_db <<EOF
INSERT INTO alerts (school_id, school_name, location, rule_name, severity, description, host_name, timestamp, resolved)
VALUES ('$SCHOOL_ID', '$SCHOOL_NAME', 'Main Server Room', 'Multiple Failed SSH Logins', 'HIGH', 'Detected 25 failed SSH login attempts in 5 minutes from a single IP.', 'server-win-01', NOW() - INTERVAL '10 minutes', false);
EOF
    
    # Medium Severity Alert
    docker exec -i "$ALERT_DB_CONTAINER" psql -U siem_user -d alert_db <<EOF
INSERT INTO alerts (school_id, school_name, location, rule_name, severity, description, host_name, timestamp, resolved)
VALUES ('$SCHOOL_ID', '$SCHOOL_NAME', 'IT Lab', 'Suspicious Process Execution', 'MEDIUM', 'A PowerShell script was executed from a temporary directory on a lab machine.', 'lab-pc-15', NOW() - INTERVAL '2 hours', false);
EOF

    # Low Severity Alert
    docker exec -i "$ALERT_DB_CONTAINER" psql -U siem_user -d alert_db <<EOF
INSERT INTO alerts (school_id, school_name, location, rule_name, severity, description, host_name, timestamp, resolved)
VALUES ('$SCHOOL_ID', '$SCHOOL_NAME', 'Reception Desk', 'Out-of-Hours Activity', 'LOW', 'Computer was powered on at 2:00 AM outside of standard school hours.', 'reception-pc', NOW() - INTERVAL '1 day', false);
EOF
else
    echo "Warning: alert-db container not found, skipping alert seeding."
fi

# Cleanup
rm "$COOKIE_FILE"

echo "=== Seeding Complete ==="
echo "Admin Login: $ADMIN_EMAIL / $ADMIN_PASS"
echo "Tech Login:  willie@school.com / password123"
