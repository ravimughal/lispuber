#!/bin/bash

ZONE="europe-southwest1-c"
STATIC_IP="34.175.36.57"

PRIMARY="nginx1-vm"
BACKUP="nginx2-vm"

BACKUP_IP="10.204.0.12"

MACHINE_IMAGE="nginx-image"



echo "Checking nginx2..."

# check HTTP status properly
CODE=$(curl -s -o /dev/null -w "%{http_code}" --max-time 2 http://$BACKUP_IP || echo "000")

echo "HTTP code: $CODE"

# get who currently has the external IP
CURRENT_OWNER=$(gcloud compute instances list \
  --filter="networkInterfaces.accessConfigs.natIP=$STATIC_IP" \
  --format="value(name)")

echo "Current IP owner: $CURRENT_OWNER"



# CASE 1: nginx2 is UP

if [[ "$CODE" == "200" || "$CODE" == "301" ]]; then
  echo "nginx2 is already UP"
  exit 0
fi



# CASE 2: nginx2 is DOWN

echo "nginx2 DOWN"


EXISTS=$(gcloud compute instances list \
  --filter="name=$BACKUP" \
  --format="value(name)")

if [[ -n "$EXISTS" ]]; then
  echo "LB2 already exists"

  STATUS=$(gcloud compute instances describe $BACKUP \
    --zone=$ZONE \
    --format="value(status)")

  if [[ "$STATUS" != "RUNNING" ]]; then
    echo "Starting LB2..."
    gcloud compute instances start $BACKUP --zone=$ZONE

    while true; do
      echo "Running check..."
      STATUS2=$(gcloud compute instances describe $BACKUP \
        --zone=$ZONE \
        --format="value(status)")

      if [[ "$STATUS2" == "RUNNING" ]]; then
        echo "LB2 Running"
        break
      fi
    done
  fi

else
  echo "Creating LB2 from machine image..."

  gcloud compute instances create $BACKUP \
    --source-machine-image=$MACHINE_IMAGE \
    --private-network-ip=10.204.0.12 \
    --tags=nginx \
    --zone=$ZONE

  echo "LB2 created"

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
          echo "Starting LB2..."
        fi
        
        if [[ "$STATUS" == "RUNNING" ]]; then
          echo "LB2 Running"
            gcloud compute instances delete-access-config $PRIMARY \
              --zone=$ZONE \
              --access-config-name="external-nat" \
              --quiet || true

          break
        fi
    fi

    echo "$BACKUP not found, retrying in 5s..."
    sleep 5
  done
fi

echo