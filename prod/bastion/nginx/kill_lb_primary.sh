echo "Killing primary LB (nginx1)..."
gcloud compute instances delete nginx1-vm --zone=europe-southwest1-c --quiet
echo "Primary LB (nginx1) killed."