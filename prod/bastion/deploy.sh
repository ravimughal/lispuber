#!/bin/bash

set -e



### CONFIG

JAR="/home/apa/projeto-ptir/prod/app/app.jar"
USER="apa"

APP_PATH="/home/apa/projeto-ptir/prod/app"
NGINX_PATH="/home/apa/projeto-ptir/prod/nginx"

echo "Getting running VMs..."

APP_VMS=$(gcloud compute instances list \
  --filter="status=RUNNING AND name~^app" \
  --format="value(name,networkInterfaces[0].networkIP)")

NGINX_VMS=$(gcloud compute instances list \
  --filter="status=RUNNING AND name~^nginx" \
  --format="value(name,networkInterfaces[0].networkIP)")

if [ -z "$APP_VMS" ]; then
  echo "No running app VMs found"
  exit 1
fi

if [ -z "$NGINX_VMS" ]; then
  echo "No running nginx VMs found"
  exit 1
fi



# 1. DEPLOY APP

echo "Deploying app to VMs..."

while read -r NAME IP; do
  echo "$NAME] Deploying..."

  scp -o StrictHostKeyChecking=no $JAR $USER@$IP:/tmp/app.jar

  ssh -o StrictHostKeyChecking=no $USER@$IP << EOF
    sudo mv /tmp/app.jar /opt/app/app.jar &&
    sudo chown apa:apa /opt/app/app.jar &&
    sudo systemctl restart app
EOF

  echo "[$NAME] Done"

done <<< "$APP_VMS"



# 2. UPDATE NGINX CONFIG

echo "Updating NGINX servers..."

while read -r NAME IP; do
  echo "$NAME] Updating nginx..."

  # Copy config files
  scp -o StrictHostKeyChecking=no \
    $NGINX_PATH/backend.conf \
    $NGINX_PATH/backend-map.conf \
    $NGINX_PATH/default \
    $USER@$IP:/tmp/

  # Copy static frontend (optional)
  scp -r -o StrictHostKeyChecking=no \
    $NGINX_PATH/nginx \
    $USER@$IP:/tmp/

  # Apply config
  ssh -o StrictHostKeyChecking=no $USER@$IP << EOF

    echo "Applying config..."

    sudo mv /tmp/backend.conf /etc/nginx/conf.d/backend.conf
    sudo mv /tmp/backend-map.conf /etc/nginx/conf.d/backend-map.conf

    # default should go to sites-available normally
    sudo mv /tmp/default /etc/nginx/sites-available/default

    # static files
    sudo cp -r /tmp/nginx/* /home/apa/nginx/

    # validate config
    sudo nginx -t

    # reload nginx
    sudo systemctl reload nginx

    echo "nginx updated"

EOF

  echo "[$NAME] Done"

done <<< "$NGINX_VMS"



# DONE

echo "Full deployment complete!"
