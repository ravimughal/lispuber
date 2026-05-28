#!/bin/bash
ZONE="europe-southwest1-c"
IMAGE="app-image"
MACHINE_TYPE="e2-small"
SUBNET="default"
VM_PREFIX="app"
MAX_SERVERS=10
BASE_IP_PREFIX="10.204.0."
START_IP_SUFFIX=20

echo "A verificar número de apps já existentes..."

EXISTING_VMS=$(gcloud compute instances list \
    --filter="zone:($ZONE) AND name ~ '^${VM_PREFIX}[0-9]+-vm'" \
    --format="value(name)")

CURRENT_COUNT=$(echo "$EXISTING_VMS" | grep -c '^' || echo 0)
echo "Servidores aplicacionais encontrados: $CURRENT_COUNT / $MAX_SERVERS"

if [ "$CURRENT_COUNT" -ge "$MAX_SERVERS" ]; then
    echo "Erro: Limite máximo de $MAX_SERVERS servidores atingido. create-app abortado."
    exit 1
fi

NEXT_NUM=1
while [ $NEXT_NUM -le $MAX_SERVERS ]; do
    TARGET_NAME="${VM_PREFIX}${NEXT_NUM}-vm"
    if ! echo "$EXISTING_VMS" | grep -q "^${TARGET_NAME}$"; then
        break
    fi
    NEXT_NUM=$((NEXT_NUM + 1))
done

VM_NAME="${VM_PREFIX}${NEXT_NUM}-vm"
NEXT_IP_SUFFIX=$((START_IP_SUFFIX + NEXT_NUM))
VM_IP="${BASE_IP_PREFIX}${NEXT_IP_SUFFIX}"

echo "Configuração da nova app:"
echo "  - Nome: $VM_NAME"
echo "  - IP Privado: $VM_IP"
echo "A criar nova instância no Gcloud: $VM_NAME"

gcloud compute instances create "$VM_NAME" \
    --zone="$ZONE" \
    --source-machine-image="$IMAGE" \
    --machine-type="$MACHINE_TYPE" \
    --subnet="$SUBNET" \
    --private-network-ip="$VM_IP" \
   --tags=app \
    --quiet

if [ $? -eq 0 ]; then
    ssh-keygen -f "/home/apa/.ssh/known_hosts" -R "$VM_IP" 2>/dev/null
else
    echo "Erro crítico ao criar a instância no Gcloud."
    exit 1
fi