DROP TABLE IF EXISTS pagamento CASCADE;
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

CREATE TABLE conforto (
    id             BIGSERIAL     PRIMARY KEY,
    nivel_conforto VARCHAR(10)   NOT NULL UNIQUE,
    preco          NUMERIC(10,2) NOT NULL CHECK (preco >= 0),

    CONSTRAINT conforto_nivel_check CHECK (nivel_conforto IN ('BASICO', 'LUXUOSO'))
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
    preco                NUMERIC(10,2) NOT NULL CHECK (preco >= 0),
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

    CHECK (fim > inicio),

    FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE,
    FOREIGN KEY (turno_id)   REFERENCES turno(id),
    FOREIGN KEY (pedido_id)  REFERENCES pedido(id)
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