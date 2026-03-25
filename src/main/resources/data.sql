-- =========================
-- UTILIZADORES
-- =========================
INSERT INTO utilizador (id, nif, email, senha, nome, genero)
VALUES
(1, '123456789', 'cliente@email.com', '123', 'Ravi', 'MASCULINO'),
(2, '987654321', 'motorista@email.com', '123', 'Joao', 'MASCULINO');

-- =========================
-- CLIENTE
-- =========================
INSERT INTO cliente (id)
VALUES (1);

-- =========================
-- MOTORISTA
-- =========================
INSERT INTO motorista (
    id,
    data_nascimento,
    rua,
    numero,
    codigo_postal,
    num_carta_conducao,
    validade_carta_conducao
)
VALUES (
    2,
    '1990-01-01',
    'Rua A',
    '10',
    '1000-000',
    'ABC123',
    '2030-01-01'
);

-- =========================
-- CONFORTO
-- =========================
INSERT INTO conforto (id, preco, nivel_conforto)
VALUES
(1, 1.0, 'BASICO'),
(2, 2.5, 'LUXUOSO');

-- =========================
-- TAXI
-- =========================
INSERT INTO taxi (
    id,
    matricula,
    modelo,
    marca,
    tipo_motor,
    ano_compra,
    conforto_id
)
VALUES
(1, 'AA-11-BB', 'Model S', 'Tesla', 'ELETRICO', 2022, 2),
(2, 'CC-22-DD', 'Clio', 'Renault', 'COMBUSTAO', 2018, 1);

-- =========================
-- TURNO
-- =========================
INSERT INTO turno (
    id,
    inicio,
    fim,
    motorista_id,
    taxi_id
)
VALUES (
    1,
    '2026-03-25 08:00:00',
    '2026-03-25 16:00:00',
    2,
    1
);

-- =========================
-- PEDIDO
-- =========================
INSERT INTO pedido (
    id,
    origem_rua,
    origem_numero,
    origem_codigo_postal,
    destino_rua,
    destino_numero,
    destino_codigo_postal,
    num_passageiros,
    conforto_id,
    cliente_id
)
VALUES (
    1,
    'Rua X',
    '10',
    '1000-001',
    'Rua Y',
    '20',
    '1000-002',
    2,
    1,
    1
);

-- =========================
-- VIAGEM
-- =========================
INSERT INTO viagem (
    id,
    origem_rua,
    origem_numero,
    origem_codigo_postal,
    destino_rua,
    destino_numero,
    destino_codigo_postal,
    km_percorridos,
    num_passageiro,
    preco,
    cliente_id,
    pedido_id,
    turno_id
)
VALUES (
    1,
    'Rua X',
    '10',
    '1000-001',
    'Rua Y',
    '20',
    '1000-002',
    12.5,
    2,
    15.0,
    1,
    1,
    1
);

-- =========================
-- FATURA
-- =========================
INSERT INTO fatura (
    id,
    num_sequencial,
    data,
    viagem_id
)
VALUES (
    1,
    1001,
    '2026-03-25 16:30:00',
    1
);