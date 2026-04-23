#!/bin/bash

USER="apa"
PROJECT_DIR="$HOME/projeto-ptir"
ZONE="europe-southwest1-c"

declare -A VM_FOLDERS=(
  [app1-vm]="app"
  [app2-vm]="app"
  [app3-vm]="app"
  [app4-vm]="app"
  [db1-vm]="db"
  [db2-vm]="db"
  [nginx1-vm]="nginx"
  [nginx2-vm]="nginx"
  [bastion1-vm]="bastion"
)

is_valid_vm() {
  [[ -n "${VM_FOLDERS[$1]}" ]]
}

print_vms() {
  echo "Valid VM mappings:"
  for vm in "${!VM_FOLDERS[@]}"; do
    echo " - $vm  ->  ${VM_FOLDERS[$vm]}"
  done
}

get_internal_ip() {
  gcloud compute instances describe "$1" \
    --zone="$ZONE" \
    --format='get(networkInterfaces[0].networkIP)'
}

pull_git() {
  cd "$PROJECT_DIR" || exit 1
  git pull https://andre-alexandre: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@github.com/ravimughal/projeto-ptir.git
  echo "Git pulled"
}

send_vm() {
  VM="$1"

  if ! is_valid_vm "$VM"; then
    echo "Invalid VM: $VM"
    print_vms
    exit 1
  fi

  FOLDER="${VM_FOLDERS[$VM]}"
  IP=$(get_internal_ip "$VM")

  if [[ -z "$IP" ]]; then
    echo "No IP for $VM"
    exit 1
  fi

  LOCAL_PATH="$PROJECT_DIR/$FOLDER"

  if [[ ! -d "$LOCAL_PATH" ]]; then
    echo "Folder not found: $LOCAL_PATH"
    exit 1
  fi

  echo "Sending $FOLDER to $VM ($USER@$IP)"

  # delete remote folder
  ssh "$USER@$IP" "rm -rf ~/$FOLDER"

  # send only folder
  scp -r "$LOCAL_PATH" "$USER@$IP:~/"

  echo "Sent $FOLDER to $VM"
}

case "$1" in

  pull)
    pull_git
    ;;

  send)
    send_vm "$2"
    ;;

  list)
    print_vms
    ;;

  *)
    echo "Usage:"
    echo "  $0 pull"
    echo "  $0 send <vm>"
    echo "  $0 list"
    ;;
esac
