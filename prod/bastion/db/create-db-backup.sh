#!/bin/bash

ZONE="europe-southwest1-c"

BACKUP="db2-vm"
BACKUP_IP="10.204.0.32"

PRIMARY_IP="10.204.0.31"
PRIMARY="db1-vm"

MACHINE_IMAGE="db-image"




echo "Checking db1..."

EXISTS1=$(gcloud compute instances list \
  --filter="name=$PRIMARY" \
  --format="value(name)")

if [[ -n "$EXISTS1" ]]; then
  echo "DB1 exists"

  STATUS1=$(gcloud compute instances describe "$PRIMARY" \
    --zone="$ZONE" \
    --format="value(status)")

  if [[ "$STATUS1" != "RUNNING" ]]; then
    echo "DB1 is not UP. DB1 must be up before running this script."
    exit 0
  fi

  echo "DB1 is RUNNING"
else
  echo "DB1 does not exist. DB1 must exist before running this script."
  exit 0
fi




echo "Checking db2..."

# check HTTP status properly
if nmap -p 5432 -T4 --host-timeout 4s --max-retries 1 --open $BACKUP_IP -oG - | grep -q "5432/open"; then
  STATE="open"
else
  STATE="closed"
fi

echo "HTTP state: $STATE"




# CASE 1: db2 is UP

if [[ "$STATE" == "open" ]]; then
  echo "db2 is UP"
  exit 0
fi



# CASE 2: db2 is DOWN

echo "db2 DOWN"

EXISTS=$(gcloud compute instances list \
  --filter="name=$BACKUP" \
  --format="value(name)")

if [[ -n "$EXISTS" ]]; then
  echo "DB2 already exists"

  STATUS=$(gcloud compute instances describe $BACKUP \
    --zone=$ZONE \
    --format="value(status)")

  if [[ "$STATUS" != "RUNNING" ]]; then
    echo "Starting DB2..."
    gcloud compute instances start $BACKUP --zone=$ZONE

    while true; do
      echo "Running check..."
      STATUS2=$(gcloud compute instances describe $BACKUP \
        --zone=$ZONE \
        --format="value(status)")

      if [[ "$STATUS2" == "RUNNING" ]]; then
        echo "DB2 Running"
        break
      fi
    done
  fi

else
  echo "Creating DB2 from machine image..."

  gcloud compute instances create db2-vm --source-machine-image=db-image --private-network-ip=10.204.0.32   --no-address --zone=europe-southwest1-c

  echo "DB2 created"

  while true; do
    echo "Running check..."

    EXISTS2=$(gcloud compute instances list \
      --filter="name=$BACKUP" \
      --format="value(name)")

    if [[ -n "$EXISTS2" ]]; then
      echo "$BACKUP exists"

        STATUS=$(gcloud compute instances describe $BACKUP \
          --zone=$ZONE \
          --format="value(status)")

        if [[ "$STATUS" != "RUNNING" ]]; then
          echo "Starting DB2..."
        fi
        
        if [[ "$STATUS" == "RUNNING" ]]; then
          echo "DB2 Running"
          break
        fi
    fi

    echo "$BACKUP not found, retrying in 5s..."
    sleep 5
  done
fi


gcloud compute instances add-tags $BACKUP \
  --tags=db \
  --zone=$ZONE

ssh-keygen -f "/home/apa/.ssh/known_hosts" -R $BACKUP_IP

# small delay to allow boot
sleep 30

echo "Running DB bootstrap via SSH..."


ssh -o StrictHostKeyChecking=no \
    -o UserKnownHostsFile=/dev/null \
    apa@$BACKUP_IP <<'EOF'

PRIMARY_IP="10.204.0.31"
REPL_USER="replicator"

echo "Stopping PostgreSQL..."
sudo systemctl stop postgresql

echo "Cleaning old data directory..."
sudo rm -rf /var/lib/postgresql/18/main
sudo mkdir -p /var/lib/postgresql/18/main
sudo chown postgres:postgres /var/lib/postgresql/18/main
sudo chown -R postgres:postgres /var/lib/postgresql/18/main
sudo chmod 700 /var/lib/postgresql/18/main
sudo chmod 700 /var/lib/postgresql/18/main/pg_wal
sudo chown -R postgres:postgres /var/lib/postgresql/18/main/pg_wal

echo "Starting base backup from PRIMARY..."

sudo PGPASSWORD="rep_pass" -u postgres pg_basebackup -h $PRIMARY_IP \
  -D /var/lib/postgresql/18/main \
  -U $REPL_USER -P -R

echo "Fixing permissions..."
sudo chown -R postgres:postgres /var/lib/postgresql/18/main

sudo sed -i "s/^#\?listen_addresses.*/listen_addresses = '*'/" /etc/postgresql/18/main/postgresql.conf

echo "host all all 0.0.0.0/0 scram-sha-256" | sudo tee -a /etc/postgresql/18/main/pg_hba.conf

echo "Starting PostgreSQL..."
sudo systemctl start postgresql

echo "Standby setup complete."
EOF