echo "Creating bastion (bastion1)..."
gcloud compute instances create bastion1-vm --source-machine-image=bastion-image --private-network-ip=10.204.0.2 --zone=europe-southwest1-c
echo "Bastion (bastion1) created."
