#!/bin/bash

set -e

case "$1" in
  start)
    gcloud compute instances start bastion1-vm --zone=europe-southwest1-c
    ;;

  stop)
    gcloud compute instances stop bastion1-vm --zone=europe-southwest1-c
    ;;

  ssh)
    gcloud compute ssh bastion1-vm --zone=europe-southwest1-c --tunnel-through-iap
    ;;

  *)
    echo "Usage: $0 {start|stop|ssh}"
    ;;
esac








