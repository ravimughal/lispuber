#!/bin/bash

ZONE="europe-southwest1-c"

if [ -z "$1" ]; then
    echo "Erro: Deves fornecer o nome da instância a terminar."
    echo "Uso: $0 <nome-da-instancia>"
    exit 1
fi

VM_NAME=$1

echo "A iniciar a remoção do servidor..."

echo "A verificar se a instância $VM_NAME já existe ..."
gcloud compute instances describe "$VM_NAME" --zone="$ZONE" >/dev/null 2>&1

if [ $? -ne 0 ]; then
    echo "Erro: A instância $VM_NAME não foi encontrada na zona $ZONE."
    exit 1
fi

echo "A eliminar a instância $VM_NAME..."
gcloud compute instances delete "$VM_NAME" \
    --zone="$ZONE" \
    --quiet

if [ $? -eq 0 ]; then
    echo "Instância $VM_NAME terminada com sucesso."
else
    echo "Erro crítico ao tentar eliminar a instância."
    exit 1
fi

echo "Processo concluído: $VM_NAME foi removida."