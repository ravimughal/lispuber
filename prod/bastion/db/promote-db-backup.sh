#!/bin/bash

BACKUP_IP="10.204.0.32"

ssh -o StrictHostKeyChecking=no \
    -o UserKnownHostsFile=/dev/null \
    apa@$BACKUP_IP <<'EOF'
    
echo "Promoting standby to primary..."

# 1. Promote using SQL (most reliable method)
sudo -u postgres psql -c "SELECT pg_promote();"

echo "Promotion triggered"

# 2. Wait for recovery exit
sleep 5

# 3. Check status
STATUS=$(sudo -u postgres psql -tAc "SELECT pg_is_in_recovery();")

if [[ "$STATUS" == "f" ]]; then
  echo "This node is now PRIMARY (read-write enabled)"
else
  echo "Still in recovery mode"
  exit 1
fi

echo "Promotion complete"
EOF