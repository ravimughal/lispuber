#!/bin/bash

ZONE="europe-southwest1-c"

PRIMARY="db1-vm"
PRIMARY_IP="10.204.0.31"

MACHINE_IMAGE="db-image"



echo "Checking db1..."

# check HTTP status properly
if nmap -p 5432 -T4 --host-timeout 4s --max-retries 1 --open $PRIMARY_IP -oG - | grep -q "5432/open"; then
  STATE="open"
else
  STATE="closed"
fi

echo "HTTP state: $STATE"




# CASE 1: db1 is UP

if [[ "$STATE" == "open" ]]; then
  echo "db1 is UP"
  exit 0
fi



# CASE 2: db1 is DOWN

echo "db1 DOWN"

EXISTS=$(gcloud compute instances list \
  --filter="name=$PRIMARY" \
  --format="value(name)")

if [[ -n "$EXISTS" ]]; then
  echo "DB1 already exists"

  STATUS=$(gcloud compute instances describe $PRIMARY \
    --zone=$ZONE \
    --format="value(status)")

  if [[ "$STATUS" != "RUNNING" ]]; then
    echo "Starting DB1..."
    gcloud compute instances start $PRIMARY --zone=$ZONE

    while true; do
      echo "Running check..."
      STATUS2=$(gcloud compute instances describe $PRIMARY \
        --zone=$ZONE \
        --format="value(status)")

      if [[ "$STATUS2" == "RUNNING" ]]; then
        echo "DB1 Running"
        break
      fi
    done
  fi

else
  echo "Creating DB1 from machine image..."

  gcloud compute instances create db1-vm --source-machine-image=db-image --private-network-ip=10.204.0.31   --no-address --zone=europe-southwest1-c

  echo "DB1 created"

  while true; do
    echo "Running check..."

    EXISTS2=$(gcloud compute instances list \
      --filter="name=$PRIMARY" \
      --format="value(name)")

    if [[ -n "$EXISTS2" ]]; then
      echo "$PRIMARY exists"

        STATUS=$(gcloud compute instances describe $PRIMARY \
          --zone=$ZONE \
          --format="value(status)")

        if [[ "$STATUS" != "RUNNING" ]]; then
          echo "Starting DB1..."
        fi

        if [[ "$STATUS" == "RUNNING" ]]; then
          echo "LB1 Running"
          break
        fi
    fi

    echo "$PRIMARY not found, retrying in 5s..."
    sleep 5
  done
fi


gcloud compute instances add-tags $PRIMARY \
  --tags=db \
  --zone=$ZONE

ssh-keygen -f "/home/apa/.ssh/known_hosts" -R $PRIMARY_IP

# small delay to allow boot
sleep 30

echo "Running DB bootstrap via SSH..."

ssh -o StrictHostKeyChecking=no \
    -o UserKnownHostsFile=/dev/null \
    apa@$PRIMARY_IP <<'EOF'

DB_NAME="ptir_db"
DB_USER="ptir_user"
DB_PASS="ptir_password"
REPL_USER="replicator"
REPL_PASS="rep_pass"
BACKUP_IP="10.204.0.32"

echo "Creating PostgreSQL user and database..."

sudo -u postgres psql <<SQL

DO \$\$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'ptir_user') THEN
      CREATE ROLE ptir_user LOGIN PASSWORD 'ptir_password';
   ELSE
      ALTER ROLE ptir_user WITH PASSWORD 'ptir_password';
   END IF;
END
\$\$;

SELECT 'CREATE DATABASE ptir_db OWNER ptir_user'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'ptir_db')
\gexec

ALTER DATABASE ptir_db OWNER TO ptir_user;

-- replication user
DO \$\$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'replicator') THEN
      CREATE ROLE replicator REPLICATION LOGIN PASSWORD 'rep_pass';
   END IF;
END
\$\$;

SQL


echo "Configuring PostgreSQL for replication..."

PG_CONF=$(ls /etc/postgresql/18/main/postgresql.conf)
HBA_CONF=$(ls /etc/postgresql/18/main/pg_hba.conf)

# enable replication settings (safe append if not exists)
sudo sed -i "s/^#\?wal_level.*/wal_level = replica/" $PG_CONF
sudo sed -i "s/^#\?max_wal_senders.*/max_wal_senders = 10/" $PG_CONF
sudo sed -i "s/^#\?wal_keep_size.*/wal_keep_size = 256MB/" $PG_CONF
sudo sed -i "s/^#\?hot_standby.*/hot_standby = on/" $PG_CONF
sudo sed -i "s/^#\?listen_addresses.*/listen_addresses = '*'/" $PG_CONF

echo "Updating pg_hba.conf..."

sudo sed -i "/host replication replicator/d" "$HBA_CONF"
echo "host replication replicator ${BACKUP_IP}/32 scram-sha-256" | sudo tee -a "$HBA_CONF"

sudo sed -i "/host all all/d" "$HBA_CONF"
echo "host all all 0.0.0.0/0 scram-sha-256" | sudo tee -a "$HBA_CONF"


echo "Restarting PostgreSQL..."
sudo systemctl restart postgresql

PGPASSWORD='ptir_password' psql -U ptir_user -d ptir_db -h localhost <<SQL

DROP TABLE IF EXISTS reabastecimento CASCADE;
DROP TABLE IF EXISTS fatura CASCADE;
DROP TABLE IF EXISTS viagem CASCADE;
DROP TABLE IF EXISTS pedido CASCADE;
DROP TABLE IF EXISTS turno CASCADE;
DROP TABLE IF EXISTS taxi CASCADE;
DROP TABLE IF EXISTS motorista CASCADE;
DROP TABLE IF EXISTS gestor CASCADE;
DROP TABLE IF EXISTS cliente CASCADE;
DROP TABLE IF EXISTS utilizador CASCADE;
DROP TABLE IF EXISTS conforto CASCADE;
DROP TABLE IF EXISTS configuracao_taxas CASCADE;
DROP TABLE IF EXISTS pagamento CASCADE;

CREATE TABLE conforto (
    id             BIGSERIAL     PRIMARY KEY,
    nivel_conforto VARCHAR(10)   NOT NULL UNIQUE,
    preco          NUMERIC(10,2) NOT NULL CHECK (preco >= 0),

    CONSTRAINT conforto_nivel_check CHECK (nivel_conforto IN ('BASICO', 'CONFORTO', 'LUXUOSO'))
);


CREATE TABLE utilizador (
    id              BIGSERIAL    PRIMARY KEY,
    nome            VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    nif             VARCHAR(9)   NOT NULL UNIQUE,
    genero          VARCHAR(15)  NOT NULL,
    senha           VARCHAR(255) NOT NULL,
    data_nascimento DATE         NOT NULL,

    CONSTRAINT utilizador_genero_check CHECK (genero IN ('MASCULINO', 'FEMININO', 'NAO_INDICADO'))
);



CREATE TABLE cliente (
    id BIGINT PRIMARY KEY,
    FOREIGN KEY (id) REFERENCES utilizador(id) ON DELETE CASCADE
);



CREATE TABLE gestor (
    id BIGINT PRIMARY KEY,
    FOREIGN KEY (id) REFERENCES utilizador(id) ON DELETE CASCADE
);



CREATE TABLE motorista (
    id                      BIGINT       PRIMARY KEY,
    num_carta_conducao      VARCHAR(31)  NOT NULL UNIQUE,
    validade_carta_conducao DATE         NOT NULL,
    rua                     VARCHAR(255) NOT NULL,
    rua_numero              INTEGER      NOT NULL,
    codigo_postal           VARCHAR(15)  NOT NULL,
    criado_em           TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em       TIMESTAMP,

    FOREIGN KEY (id) REFERENCES utilizador(id) ON DELETE CASCADE
);



CREATE TABLE taxi (
    id                  BIGSERIAL     PRIMARY KEY,
    matricula           VARCHAR(31)   NOT NULL UNIQUE,
    marca               VARCHAR(63)   NOT NULL,
    modelo              VARCHAR(63)   NOT NULL,
    tipo_motor          VARCHAR(10)   NOT NULL,
    ano_compra          INTEGER       NOT NULL CHECK (ano_compra > 1900),
    conforto_id         BIGINT        NOT NULL,
    kilometros          NUMERIC(10,2) NOT NULL CHECK (kilometros >= 0),
    criado_em           TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em       TIMESTAMP,

    CONSTRAINT taxi_tipo_motor_check CHECK (tipo_motor IN ('COMBUSTAO', 'ELETRICO')),

    FOREIGN KEY (conforto_id) REFERENCES conforto(id)
);



CREATE TABLE turno (
    id           BIGSERIAL PRIMARY KEY,
    motorista_id BIGINT    NOT NULL,
    taxi_id      BIGINT,
    inicio       TIMESTAMP NOT NULL,
    fim          TIMESTAMP NOT NULL,

    CHECK (fim > inicio),

    FOREIGN KEY (motorista_id) REFERENCES motorista(id) ON DELETE CASCADE,
    FOREIGN KEY (taxi_id)      REFERENCES taxi(id)      ON DELETE CASCADE
);



CREATE TABLE pedido (
    id                   BIGSERIAL PRIMARY KEY,
    cliente_id           BIGINT        NOT NULL,
    conforto_id          BIGINT        NOT NULL,
    motorista_id         BIGINT,

    num_passageiros      INTEGER       NOT NULL CHECK (num_passageiros > 0),
    estado_pedido        VARCHAR(20)   NOT NULL DEFAULT 'PENDENTE',
    data_solicitacao     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    rua_origem           VARCHAR(255),
    latitude_origem      NUMERIC(9,6)  NOT NULL,
    longitude_origem     NUMERIC(9,6)  NOT NULL,

    rua_destino          VARCHAR(255),
    latitude_destino     NUMERIC(9,6)  NOT NULL,
    longitude_destino    NUMERIC(9,6)  NOT NULL,

    FOREIGN KEY (cliente_id)  REFERENCES cliente(id)  ON DELETE CASCADE,
    FOREIGN KEY (conforto_id) REFERENCES conforto(id),
    FOREIGN KEY (motorista_id) REFERENCES motorista(id) ON DELETE SET NULL
);

CREATE TABLE configuracao_taxas (
    chave VARCHAR(50) PRIMARY KEY,
    valor DECIMAL(10, 2) NOT NULL
);

CREATE TABLE viagem (
    id                   BIGSERIAL PRIMARY KEY,
    cliente_id           BIGINT    NOT NULL,
    turno_id             BIGINT    NOT NULL,
    pedido_id            BIGINT    NOT NULL,

    inicio               TIMESTAMP NOT NULL,
    fim                  TIMESTAMP,

    km_percorridos       NUMERIC(10,2) NOT NULL CHECK (km_percorridos >= 0),

    preco                NUMERIC(10,2) NOT NULL CHECK (preco >= 0),


    CHECK (fim > inicio),

    FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE,
    FOREIGN KEY (turno_id)   REFERENCES turno(id),
    FOREIGN KEY (pedido_id)  REFERENCES pedido(id)
);



CREATE TABLE avaliacao (
    id              BIGINT     PRIMARY KEY,
    cliente_id      BIGINT    NOT NULL,
    viagem_id       BIGINT    NOT NULL,
    motorista_id    BIGINT    NOT NULL,
    classificacao   INTEGER   NOT NULL CHECK (classificacao >= 1 AND classificacao <= 5),
    comentario      VARCHAR(500),
    criado_em       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE,
    FOREIGN KEY (viagem_id) REFERENCES viagem(id) ON DELETE CASCADE,
    FOREIGN KEY (motorista_id) REFERENCES motorista(id) ON DELETE CASCADE
);



CREATE TABLE fatura (
    id             BIGSERIAL PRIMARY KEY,
    viagem_id      BIGINT    NOT NULL UNIQUE,
    num_sequencial BIGINT    NOT NULL UNIQUE,
    data           TIMESTAMP NOT NULL,

    FOREIGN KEY (viagem_id) REFERENCES viagem(id) ON DELETE CASCADE
);



CREATE TABLE reabastecimento (
    id              BIGSERIAL PRIMARY KEY,
    turno_id        BIGINT    NOT NULL,

    data_inicio     TIMESTAMP NOT NULL,
    data_fim        TIMESTAMP NOT NULL,

    quantidade      NUMERIC(10,2) NOT NULL CHECK (quantidade >= 0),
    euros_pagos     NUMERIC(10,2) NOT NULL CHECK (euros_pagos >= 0),
    kilometros      NUMERIC(10,2) NOT NULL CHECK (kilometros >= 0),

    CHECK (data_fim > data_inicio),

    FOREIGN KEY (turno_id) REFERENCES turno(id) ON DELETE CASCADE
);



CREATE TABLE pagamento (
    id                  BIGSERIAL     PRIMARY KEY,
    viagem_id           BIGINT        NOT NULL UNIQUE,
    stripe_payment_id   VARCHAR(255)  NOT NULL UNIQUE,
    
    estado              VARCHAR(20)   NOT NULL DEFAULT 'PENDENTE',
    valor               NUMERIC(10,2) NOT NULL,
    criado_em           TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em       TIMESTAMP,

    CONSTRAINT pagamento_estado_check CHECK (estado IN ('PENDENTE', 'PAGO')),
    
    FOREIGN KEY (viagem_id) REFERENCES viagem(id) ON DELETE CASCADE
);



INSERT INTO configuracao_taxas (chave, valor) 
VALUES ('TAXA_NOTURNA', 1.30);



INSERT INTO conforto (nivel_conforto, preco) VALUES
    ('BASICO', 0.50),
    ('CONFORTO', 0.75),
    ('LUXUOSO', 1.0);

SQL

echo "Done."
EOF