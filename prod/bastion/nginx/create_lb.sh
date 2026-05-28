#!/bin/bash

ZONE="europe-southwest1-c"
STATIC_IP="34.175.36.57"

PRIMARY="nginx1-vm"
BACKUP="nginx2-vm"

PRIMARY_IP="10.204.0.11"

MACHINE_IMAGE="nginx-image"



echo "Checking nginx1..."

# check HTTP status properly
CODE=$(curl -s -o /dev/null -w "%{http_code}" --max-time 2 http://$PRIMARY_IP || echo "000")

echo "HTTP code: $CODE"

# get who currently has the external IP
CURRENT_OWNER=$(gcloud compute instances list \
  --filter="networkInterfaces.accessConfigs.natIP=$STATIC_IP" \
  --format="value(name)")

echo "Current IP owner: $CURRENT_OWNER"



# CASE 1: nginx1 is UP

if [[ "$CODE" == "200" || "$CODE" == "301" ]]; then
  echo "nginx1 is UP"

  if [[ "$CURRENT_OWNER" == "$PRIMARY" ]]; then
    echo "IP already on nginx1"
    exit 0
  fi

  echo "Moving IP back to nginx1..."

  # remove from backup
  gcloud compute instances delete-access-config $BACKUP \
    --zone=$ZONE \
    --access-config-name="external-nat" \
    --quiet || true

  # remove from primary
  gcloud compute instances delete-access-config $PRIMARY \
    --zone=$ZONE \
    --access-config-name="external-nat" \
    --quiet || true

  # attach to primary
  gcloud compute instances add-access-config $PRIMARY \
    --zone=$ZONE \
    --access-config-name="external-nat" \
    --address=$STATIC_IP \
    --quiet

  echo "Failback complete"
  exit 0
fi



# CASE 2: nginx1 is DOWN

echo "nginx1 DOWN"

if [[ "$CURRENT_OWNER" == "$BACKUP" ]]; then
  gcloud compute instances delete-access-config $BACKUP \
    --zone=$ZONE \
    --access-config-name="external-nat" \
    --quiet || true
fi


EXISTS=$(gcloud compute instances list \
  --filter="name=$PRIMARY" \
  --format="value(name)")

if [[ -n "$EXISTS" ]]; then
  echo "LB1 already exists"

  STATUS=$(gcloud compute instances describe $PRIMARY \
    --zone=$ZONE \
    --format="value(status)")

  if [[ "$STATUS" != "RUNNING" ]]; then
    echo "Starting LB1..."
    gcloud compute instances start $PRIMARY --zone=$ZONE
  fi

else
  echo "Creating LB1 from machine image..."

  gcloud compute instances create $PRIMARY \
    --source-machine-image=$MACHINE_IMAGE \
    --private-network-ip=10.204.0.11 \
    --tags=nginx \
    --zone=$ZONE

  echo "LB1 created"

  while true; do
    echo "Running check..."

    EXISTS2=$(gcloud compute instances list \
      --filter="name=$PRIMARY" \
      --format="value(name)")

    if [[ -n "$EXISTS2" ]]; then
      echo "$PRIMARY exists"

        STATUS=$(gcloud compute instances describe $PRIMARY \
          --zone=$ZONE \
          --format="value(status)")

        if [[ "$STATUS" != "RUNNING" ]]; then
          echo "Starting LB1..."
        fi
        
        if [[ "$STATUS" == "RUNNING" ]]; then
          echo "LB1 Running"
            # attach to primary
            gcloud compute instances delete-access-config $PRIMARY \
            --zone=$ZONE \
            --access-config-name="external-nat" \
            --quiet || true

          gcloud compute instances add-access-config $PRIMARY \
            --zone=$ZONE \
            --access-config-name="external-nat" \
            --address=$STATIC_IP \
            --quiet || true

          break
        fi
    fi

    echo "$PRIMARY not found, retrying in 5s..."
    sleep 5
  done
fi


