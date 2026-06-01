#!/bin/bash

ZONE="europe-southwest1-c"

gcloud compute instances delete bastion1-vm --zone="$ZONE" --quiet &
gcloud compute instances delete nginx1-vm --zone="$ZONE" --quiet &
gcloud compute instances delete nginx2-vm --zone="$ZONE" --quiet &
gcloud compute instances delete app1-vm --zone="$ZONE" --quiet &
gcloud compute instances delete app2-vm --zone="$ZONE" --quiet &
gcloud compute instances delete app3-vm --zone="$ZONE" --quiet &
gcloud compute instances delete app4-vm --zone="$ZONE" --quiet &
gcloud compute instances delete app5-vm --zone="$ZONE" --quiet &
gcloud compute instances delete app6-vm --zone="$ZONE" --quiet &
gcloud compute instances delete app7-vm --zone="$ZONE" --quiet &
gcloud compute instances delete app8-vm --zone="$ZONE" --quiet &
gcloud compute instances delete app9-vm --zone="$ZONE" --quiet &
gcloud compute instances delete app10-vm --zone="$ZONE" --quiet &
gcloud compute instances delete db1-vm --zone="$ZONE" --quiet &
gcloud compute instances delete db2-vm --zone="$ZONE" --quiet &

wait

echo "All instance deletion requests completed."
apa@DESKTOP-9U77GD3:~$



