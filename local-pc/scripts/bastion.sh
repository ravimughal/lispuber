#!/bin/bash

bastion() {
  case "$1" in
    start)
      gcloud compute instances start apa@bastion1-vm --zone=europe-southwest1-c
      ;;
    stop)
      gcloud compute instances stop apa@bastion1-vm --zone=europe-southwest1-c
      ;;
    ssh)
      gcloud compute ssh apa@bastion1-vm --zone=europe-southwest1-c --tunnel-through-iap
      ;;
    *)
      echo "Usage: bastion {start|stop|ssh}"
      ;;
  esac
}