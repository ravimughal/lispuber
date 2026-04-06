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
    role            VARCHAR(255) NOT NULL,

    CONSTRAINT utilizador_genero_check CHECK (genero IN ('MASCULINO', 'FEMININO', 'NAO_INDICADO'))
    CONSTRAINT utilizador_role_check CHECK (role IN ('CLIENTE', 'GESTOR', 'MOTORISTA'))
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
    id          BIGSERIAL     PRIMARY KEY,
    matricula   VARCHAR(31)   NOT NULL UNIQUE,
    marca       VARCHAR(63)   NOT NULL,
    modelo      VARCHAR(63)   NOT NULL,
    tipo_motor  VARCHAR(10)   NOT NULL,
    ano_compra  INTEGER       NOT NULL CHECK (ano_compra > 1900),
    conforto_id BIGINT        NOT NULL,
    kilometros  NUMERIC(10,2) NOT NULL CHECK (kilometros >= 0),

    CONSTRAINT taxi_tipo_motor_check CHECK (tipo_motor IN ('COMBUSTAO', 'ELETRICO')),

    FOREIGN KEY (conforto_id) REFERENCES conforto(id)
);



CREATE TABLE turno (
    id           BIGSERIAL PRIMARY KEY,
    motorista_id BIGINT    NOT NULL,
    taxi_id      BIGINT    NOT NULL,
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

    num_passageiros      INTEGER       NOT NULL CHECK (num_passageiros > 0),
    preco                NUMERIC(10,2) NOT NULL CHECK (preco >= 0),

    rua_origem           VARCHAR(255),
    latitude_origem      NUMERIC(9,6)  NOT NULL,
    longitude_origem     NUMERIC(9,6)  NOT NULL,

    rua_destino          VARCHAR(255),
    latitude_destino     NUMERIC(9,6)  NOT NULL,
    longitude_destino    NUMERIC(9,6)  NOT NULL,

    FOREIGN KEY (cliente_id)  REFERENCES cliente(id)  ON DELETE CASCADE,
    FOREIGN KEY (conforto_id) REFERENCES conforto(id)
);



CREATE TABLE viagem (
    id                   BIGSERIAL PRIMARY KEY,
    cliente_id           BIGINT    NOT NULL,
    turno_id             BIGINT    NOT NULL,
    pedido_id            BIGINT    NOT NULL,

    inicio               TIMESTAMP NOT NULL,
    fim                  TIMESTAMP NOT NULL,

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