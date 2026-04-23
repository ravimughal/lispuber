#!/bin/bash

ZONE="europe-southwest1-c"
IMAGE="app-image"

# Apps
APP1_IP="10.204.0.21"
APP2_IP="10.204.0.22"

# Backups
APP3_NAME="app3-vm"
APP4_NAME="app4-vm"

APP3_IP="10.204.0.23"
APP4_IP="10.204.0.24"



echo "Checking app health..."

# function to check app
check_app() {
  IP=$1
  CODE=$(curl -s -o /dev/null -w "%{http_code}" --max-time 2 http://$IP:8080/auth/debug || echo "000")
  echo $CODE
}

# function to check if VM exists
vm_exists() {
  gcloud compute instances describe $1 --zone=$ZONE >/dev/null 2>&1
}



# APP1

CODE1=$(check_app $APP1_IP)
echo "app1 status: $CODE1"

if [[ "$CODE1" != "200" ]]; then
  echo "app1 DOWN → ensure app3 exists"

  if vm_exists $APP3_NAME; then
    echo "app3 already running"
  else
    echo "Creating app3..."
    gcloud compute instances create $APP3_NAME \
      --source-machine-image=$IMAGE \
      --private-network-ip=$APP3_IP \
      --no-address \
      --zone=$ZONE \
      --quiet
  fi

else
  echo "app1 UP → remove app3 if exists"

  if vm_exists $APP3_NAME; then
    echo "🗑 Deleting app3..."
    gcloud compute instances delete $APP3_NAME \
      --zone=$ZONE \
      --quiet
  else
    echo "app3 not present"
  fi
fi



# APP2

CODE2=$(check_app $APP2_IP)
echo "app2 status: $CODE2"

if [[ "$CODE2" != "200" ]]; then
  echo "app2 DOWN → ensure app4 exists"

  if vm_exists $APP4_NAME; then
    echo "app4 already running"
  else
    echo "Creating app4..."
    gcloud compute instances create $APP4_NAME \
      --source-machine-image=$IMAGE \
      --private-network-ip=$APP4_IP \
      --no-address \
      --zone=$ZONE \
      --quiet
  fi

else
  echo "app2 UP → remove app4 if exists"

  if vm_exists $APP4_NAME; then
    echo "Deleting app4..."
    gcloud compute instances delete $APP4_NAME \
      --zone=$ZONE \
      --quiet
  else
    echo "app4 not present"
  fi
fi

echo "Done"
