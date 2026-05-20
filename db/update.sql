
DROP TABLE IF EXISTS viagem CASCADE;
DROP TABLE IF EXISTS pedido CASCADE;

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