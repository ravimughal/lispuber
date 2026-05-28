echo "Killing primary DB (db1)..."
gcloud compute instances delete db1-vm --zone=europe-southwest1-c --quiet
echo "Primary DB (db1) killed."