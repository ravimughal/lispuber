#!/bin/bash
# =============================================================================
# pg_ha.sh — PostgreSQL HA failover/failback script
#
# Setup no bastion (cron every 5 min):
#   */5 * * * * /home/apa/pg_ha.sh >> /home/apa/pg_ha/pg_ha.log 2>&1
#
# Pré-requisitos:
#   - ssh apa@<ip> sem password (já configurado)
#   - .pgpass em /var/lib/postgresql/.pgpass em ambas as VMs (ver README abaixo)
#   - apa tem sudo NOPASSWD para pg_ctlcluster, systemctl, etc.
#
# .pgpass no db1: 10.204.0.32:5432:replication:replicator:replicator_password
# .pgpass no db2: 10.204.0.31:5432:replication:replicator:replicator_password
# =============================================================================

# ── CONFIG ────────────────────────────────────────────────────────────────────
DB1_HOST="10.204.0.31"
DB2_HOST="10.204.0.32"
SSH_USER="apa"

PG_USER="postgres"
PG_PORT=5432
PG_VERSION="18"
PG_CLUSTER="main"
PG_DATA="/var/lib/postgresql/18/main"
PG_DB="ptir_db"
REPL_USER="replicator"

# Ficheiro de estado no bastion
STATE_FILE="/home/apa/db-failover/state"

# Lock file
LOCK_FILE="/tmp/pg_ha.lock"

# Email de alerta (deixa "" para desativar)
ALERT_EMAIL=""
# ── END CONFIG ────────────────────────────────────────────────────────────────

TS="[$(date '+%Y-%m-%d %H:%M:%S')]"
log()  { echo "$TS INFO  $*"; }
warn() { echo "$TS WARN  $*"; }
err()  { echo "$TS ERROR $*"; }

# ── LOCK ─────────────────────────────────────────────────────────────────────
if ! mkdir "$LOCK_FILE" 2>/dev/null; then
    warn "Já existe uma execução em curso. A sair."
    exit 0
fi
trap 'rm -rf "$LOCK_FILE"' EXIT

# ── HELPERS ──────────────────────────────────────────────────────────────────
ssh_run() {
    ssh -o ConnectTimeout=5 \
        -o StrictHostKeyChecking=no \
        -o BatchMode=yes \
        "${SSH_USER}@$1" "$2"
}

pg_query() {
    ssh_run "$1" "sudo -u ${PG_USER} psql -p ${PG_PORT} -d ${PG_DB} -tAc \"$2\"" 2>/dev/null
}

is_postgres_up() {
    ssh_run "$1" "pg_isready -h localhost -p ${PG_PORT} -q" 2>/dev/null
    return $?
}

is_primary() {
    RESULT=$(pg_query "$1" "SELECT pg_is_in_recovery();")
    [[ "$RESULT" == "f" ]]
}

notify() {
    log "ALERTA: $1 — $2"
    if [[ -n "$ALERT_EMAIL" ]]; then
        echo "$2" | mail -s "[pg_ha] $1" "$ALERT_EMAIL" 2>/dev/null || true
    fi
}

read_state() {
    mkdir -p "$(dirname "$STATE_FILE")"
    cat "$STATE_FILE" 2>/dev/null || echo "DB1_PRIMARY"
}

write_state() {
    mkdir -p "$(dirname "$STATE_FILE")"
    echo "$1" > "$STATE_FILE"
    log "Estado guardado: $1"
}

clone_and_start() {
    # clone_and_start <destino> <fonte>
    # Clona BD da <fonte> para <destino> e arranca como standby
    local DEST="$1"
    local SRC="$2"

    log "A limpar data directory em ${DEST}..."
    ssh_run "$DEST" "sudo systemctl stop postgresql || true"
    sleep 3
    ssh_run "$DEST" "sudo rm -rf ${PG_DATA} && sudo mkdir -p ${PG_DATA} && sudo chown ${PG_USER}:${PG_USER} ${PG_DATA} && sudo chmod 700 ${PG_DATA}"

    log "A clonar ${SRC} → ${DEST} via pg_basebackup..."
    ssh_run "$DEST" "sudo -u ${PG_USER} pg_basebackup \
        -h ${SRC} \
        -p ${PG_PORT} \
        -U ${REPL_USER} \
        -D ${PG_DATA} \
        -Xs -P -R"

    if [[ $? -ne 0 ]]; then
        err "pg_basebackup falhou (${SRC} → ${DEST})."
        return 1
    fi

    # Corrigir permissões após clone (pg_basebackup pode deixar 0750)
    log "A corrigir permissões..."
    ssh_run "$DEST" "sudo chmod 700 ${PG_DATA} && sudo chown -R ${PG_USER}:${PG_USER} ${PG_DATA}"

    log "A arrancar PostgreSQL em ${DEST}..."
    ssh_run "$DEST" "sudo systemctl start postgresql"
    sleep 8

    if is_postgres_up "$DEST"; then
        log "✓ PostgreSQL a correr em ${DEST}."
        return 0
    else
        err "PostgreSQL não arrancou em ${DEST}."
        ssh_run "$DEST" "sudo journalctl -u postgresql@${PG_VERSION}-${PG_CLUSTER} --no-pager -n 10" 2>/dev/null
        return 1
    fi
}

# ── FAILOVER: db1 caiu → promover db2 ────────────────────────────────────────
do_failover() {
    warn "db1 está DOWN. A promover db2 a primário..."

    ssh_run "$DB2_HOST" "sudo pg_ctlcluster ${PG_VERSION} ${PG_CLUSTER} promote"
    sleep 5

    if is_postgres_up "$DB2_HOST" && is_primary "$DB2_HOST"; then
        log "✓ db2 promovido com sucesso a primário."
        write_state "DB2_PRIMARY"
        notify "FAILOVER concluído" "db2 é agora o primário. db1 está DOWN."
    else
        err "Falha ao promover db2. Intervenção manual necessária."
        notify "FAILOVER FALHOU" "Não foi possível promover db2 a primário."
    fi
}

# ── FAILBACK: db1 voltou → resync e recolocar como primário ──────────────────
do_failback() {
    log "db1 voltou. A iniciar failback..."

    # 1. Clonar db2 → db1 (db1 fica como standby do db2)
    clone_and_start "$DB1_HOST" "$DB2_HOST" || {
        notify "FAILBACK FALHOU" "pg_basebackup de db2 → db1 falhou."
        return 1
    }

    # 2. Aguardar sincronização do db1
    log "A aguardar que db1 fique sincronizado com db2..."
    for i in $(seq 1 12); do
        LAG=$(pg_query "$DB1_HOST" \
            "SELECT COALESCE(EXTRACT(EPOCH FROM (now() - pg_last_xact_replay_timestamp()))::int, 9999);")
        LAG=${LAG//[^0-9]/}
        log "Lag do db1: ${LAG:-?}s"
        if [[ -n "$LAG" && "$LAG" -le 5 ]]; then
            log "db1 sincronizado (lag ${LAG}s)."
            break
        fi
        sleep 5
    done

    # 3. Promover db1 a primário
    log "A promover db1 a primário..."
    ssh_run "$DB1_HOST" "sudo pg_ctlcluster ${PG_VERSION} ${PG_CLUSTER} promote"
    sleep 5

    if ! is_primary "$DB1_HOST"; then
        err "db1 não foi promovido — ainda em recovery. Failback abortado."
        notify "FAILBACK FALHOU" "db1 ainda em recovery após promote."
        return 1
    fi
    log "✓ db1 é novamente primário."

    # 4. Clonar db1 → db2 (db2 fica como standby do db1)
    clone_and_start "$DB2_HOST" "$DB1_HOST" || {
        err "db1 é primário mas db2 não ficou como standby."
        notify "FAILBACK PARCIAL" "db1 é primário mas db2 falhou como standby."
        write_state "DB1_PRIMARY"
        return 1
    }

    log "✓ db2 está de volta como standby."
    write_state "DB1_PRIMARY"
    notify "FAILBACK concluído" "Tudo normal: db1=primário, db2=standby."
}

# ── MAIN ─────────────────────────────────────────────────────────────────────
CURRENT_STATE=$(read_state)
log "=== Verificação pg_ha | estado=${CURRENT_STATE} ==="

DB1_UP=false
DB2_UP=false
is_postgres_up "$DB1_HOST" && DB1_UP=true
is_postgres_up "$DB2_HOST" && DB2_UP=true

log "db1 acessível: ${DB1_UP} | db2 acessível: ${DB2_UP}"

case "$CURRENT_STATE" in

    DB1_PRIMARY)
        if $DB1_UP; then
            if is_primary "$DB1_HOST"; then
                log "db1 saudável como primário. Nada a fazer."
            else
                warn "db1 UP mas em recovery — inesperado. A aguardar próxima verificação."
            fi
        else
            warn "db1 está DOWN (estado era DB1_PRIMARY)."
            if $DB2_UP; then
                do_failover
            else
                err "AMBOS db1 e db2 estão DOWN. Intervenção manual necessária!"
                notify "CRÍTICO" "db1 e db2 ambos inacessíveis!"
            fi
        fi
        ;;

    DB2_PRIMARY)
        if ! $DB2_UP; then
            err "db2 está DOWN mas é o primário atual. Intervenção manual necessária!"
            notify "CRÍTICO" "db2 (primário ativo) está DOWN!"
        elif $DB1_UP; then
            log "db1 voltou enquanto db2 era primário. A iniciar failback..."
            do_failback
        else
            log "db2 saudável como primário. db1 ainda DOWN. Nada a fazer."
        fi
        ;;

    *)
        err "Estado desconhecido '${CURRENT_STATE}'. A repor para DB1_PRIMARY."
        write_state "DB1_PRIMARY"
        ;;
esac

log "=== Fim da verificação pg_ha ==="