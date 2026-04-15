#!/bin/bash

BASTION="bastion1-vm"
ZONE="europe-southwest1-c"
KEY="$HOME/.ssh/google_compute_engine"
LOCAL_PORT=2222


# GET INTERNAL IP
get_ip() {
  gcloud compute instances describe "$1" \
    --zone="$ZONE" \
    --format='get(networkInterfaces[0].networkIP)'
}


# START IAP TUNNEL
start_tunnel() {
  echo "Starting IAP tunnel to $BASTION..."

  gcloud compute start-iap-tunnel "$BASTION" 22 \
    --zone="$ZONE" \
    --local-host-port=localhost:$LOCAL_PORT
}


# SSH FORWARD (ANY VM)
# usage: forward app1-vm 8080 8080
forward() {
  VM="$1"
  LOCAL_PORT_FWD="$2"
  REMOTE_PORT="$3"

  if [[ -z "$VM" ]]; then
    echo "Usage: $0 forward <vm> <local_port> <remote_port>"
    exit 1
  fi

  echo "Forwarding $VM $LOCAL_PORT_FWD → $REMOTE_PORT"

  ssh -i "$KEY" \
    -o StrictHostKeyChecking=no \
    -p $LOCAL_PORT \
    -L $LOCAL_PORT_FWD:$VM:$REMOTE_PORT \
    apa@localhost
}


# GENERIC SSHFS MOUNT
# usage: mount app1-vm
mount_vm() {
  VM="$1"

  if [[ -z "$VM" ]]; then
    echo "Usage: $0 mount <vm>"
    exit 1
  fi

  IP=$(get_ip "$VM")

  if [[ -z "$IP" ]]; then
    echo "Cannot get IP for $VM"
    exit 1
  fi

  MNT="$HOME/mnt/$VM"

  mkdir -p "$MNT"

  echo "Mounting $VM ($IP) → $MNT"

  sudo sshfs \
    -o IdentityFile="$KEY" \
    -o StrictHostKeyChecking=no \
    -o allow_other \
    -o "ProxyCommand=ssh -p $LOCAL_PORT -i $KEY -o StrictHostKeyChecking=no -W %h:%p apa@localhost" \
    "apa@$IP:/home/apa" "$MNT"
}

unmount_vm() {
  VM="$1"

  if [[ -z "$VM" ]]; then
    echo "Usage: $0 unmount <vm>"
    exit 1
  fi

  MNT="$HOME/mnt/$VM"

  echo "Unmounting $MNT"

  if mountpoint -q "$MNT"; then
    sudo umount "$MNT" 2>/dev/null || sudo umount -l "$MNT"
    echo "Unmounted $VM"
  else
    echo "Not mounted: $MNT"
  fi
}



case "$1" in

  start)
    start_tunnel
    ;;

  forward)
    forward "$2" "$3" "$4"
    ;;

  mount)
    mount_vm "$2"
    ;;

  umount)
    unmount_vm "$2"
    ;;

  *)
    echo "Usage:"
    echo "  $0 start"
    echo "  $0 forward <vm> <local_port> <remote_port>"
    echo "  $0 mount <vm>"
    echo "  $0 umount <vm>"
    ;;
esac