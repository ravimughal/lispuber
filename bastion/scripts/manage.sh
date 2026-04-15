#!/bin/bash

ZONE="europe-southwest1-c"
USER="apa"

VMS=(
  app1-vm
  app2-vm
  db1-vm
  db2-vm
  nginx1-vm
  nginx2-vm
)

is_valid_vm() {
  for vm in "${VMS[@]}"; do
    if [[ "$vm" == "$1" ]]; then
      return 0
    fi
  done
  return 1
}

print_vms() {
  echo "Valid VM names:"
  for vm in "${VMS[@]}"; do
    echo " - $vm"
  done
}

run_vm_cmd() {
  ACTION=$1
  VM=$2

  if ! is_valid_vm "$VM"; then
    echo "Invalid VM name: $VM"
    print_vms
    exit 1
  fi

  gcloud compute instances "$ACTION" "$VM" --zone="$ZONE"
}

get_ip() {
  gcloud compute instances describe "$1" \
    --zone="$ZONE" \
    --format='get(networkInterfaces[0].networkIP)'
}

ssh_ip() {
  VM=$1

  if ! is_valid_vm "$VM"; then
    echo "Invalid VM name: $VM"
    print_vms
    exit 1
  fi

  IP=$(get_ip "$VM")

  if [[ -z "$IP" ]]; then
    echo "No internal IP for $VM"
    exit 1
  fi

  echo "Connecting to $VM - $USER@$IP"
  ssh "$USER@$IP"
}

case "$1" in

  start-all)
    for vm in "${VMS[@]}"; do
      echo "starting $vm"
      gcloud compute instances start "$vm" --zone="$ZONE"
    done
    ;;

  stop-all)
    for vm in "${VMS[@]}"; do
      echo "stopping $vm"
      gcloud compute instances stop "$vm" --zone="$ZONE"
    done
    ;;

  start)
    run_vm_cmd start "$2"
    ;;

  stop)
    run_vm_cmd stop "$2"
    ;;

  ssh)
    ssh_ip "$2"
    ;;

  list)
    echo ""
    gcloud compute instances list
    ;;

  *)
    echo "Usage:"
    echo "  $0 start-all"
    echo "  $0 stop-all"
    echo "  $0 start <vm>"
    echo "  $0 stop <vm>"
    echo "  $0 ssh <vm>"
    echo "  $0 list"
    echo ""
    print_vms
    ;;
esac