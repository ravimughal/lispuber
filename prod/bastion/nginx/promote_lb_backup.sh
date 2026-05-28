#!/bin/bash

ZONE="europe-southwest1-c"
STATIC_IP="34.175.36.57"

PRIMARY="nginx1-vm"
BACKUP="nginx2-vm"

echo "Moving IP to nginx2..."

# remove from primary
gcloud compute instances delete-access-config $PRIMARY \
  --zone=$ZONE \
  --access-config-name="external-nat" \
  --quiet || true

# attach to backup
gcloud compute instances delete-access-config $BACKUP \
  --zone=$ZONE \
  --access-config-name="external-nat" \
  --quiet || true
  
# attach to backup
gcloud compute instances add-access-config $BACKUP \
  --zone=$ZONE \
  --access-config-name="external-nat" \
  --address=$STATIC_IP \
  --quiet

echo "Failover complete"
