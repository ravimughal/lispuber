-- CONFORTO  (2 níveis permitidos pelo schema atual;
--            descomente o ALTER TABLE acima para o 3.º)
INSERT INTO conforto (nivel_conforto, preco) VALUES
    ('BASICO', 0.50),
    ('CONFORTO', 0.75),
    ('LUXUOSO', 1.0);


-- UTILIZADOR
INSERT INTO utilizador (nome, email, nif, genero, senha, data_nascimento) VALUES
    ('Ana Ferreira',     'ana.ferreira@email.pt',     '123456789', 'FEMININO',    'hash_senha_1', '1990-03-15'),
    ('Bruno Costa',      'bruno.costa@email.pt',      '234567890', 'MASCULINO',   'hash_senha_2', '1985-07-22'),
    ('Carla Mendes',     'carla.mendes@email.pt',     '345678901', 'FEMININO',    'hash_senha_3', '1992-11-08'),
    ('Diogo Santos',     'diogo.santos@email.pt',     '456789012', 'MASCULINO',   'hash_senha_4', '1978-05-30'),
    ('Eva Rodrigues',    'eva.rodrigues@email.pt',    '567890123', 'FEMININO',    'hash_senha_5', '1995-01-20'),
    ('Filipe Oliveira',  'filipe.oliveira@email.pt',  '678901234', 'MASCULINO',   'hash_senha_6', '1982-09-14'),
    ('Gabriela Nunes',   'gabriela.nunes@email.pt',   '789012345', 'FEMININO',    'hash_senha_7', '1988-04-03'),
    ('Hugo Marques',     'hugo.marques@email.pt',     '890123456', 'MASCULINO',   'hash_senha_8', '1975-12-25'),
    ('Inês Carvalho',    'ines.carvalho@email.pt',    '901234567', 'FEMININO',    'hash_senha_9', '1999-06-18'),
    ('João Pereira',     'joao.pereira@email.pt',     '012345678', 'NAO_INDICADO','hash_senha_10','2000-02-29'),
    ('João Malaqueijo',  'joao.malaqueijo@email.pt',  '012345679', 'NAO_INDICADO','hash_senha_11','2000-02-29');


-- CLIENTE  (ids 1, 3, 5, 7, 9 → Ana, Carla, Eva, Gabriela, Inês)
INSERT INTO cliente (id) VALUES (1), (3), (5), (7), (9);


-- GESTOR  (ids 2, 4 → Bruno, Diogo)
INSERT INTO gestor (id) VALUES (2), (4);


-- MOTORISTA  (ids 6, 8, 10 → Filipe, Hugo, João)
INSERT INTO motorista (id, num_carta_conducao, validade_carta_conducao, rua, rua_numero, codigo_postal) VALUES
    (6,  'PT-A-123456', '2027-08-31', 'Rua das Flores',       12,  '1200-195'),
    (8,  'PT-B-654321', '2026-03-15', 'Avenida da Liberdade', 100, '1250-096'),
    (10, 'PT-C-987654', '2028-11-30', 'Praça do Comércio',    5,   '1100-148'),
    (11, 'PT-H-987654', '2028-11-30', 'Praça do Comércio',    5,   '1100-148');


-- TAXI
INSERT INTO taxi (matricula, marca, modelo, tipo_motor, ano_compra, conforto_id, kilometros) VALUES
    ('AA-00-BB', 'Toyota',     'Corolla',    'COMBUSTAO', 2019, 1,  45000.00),
    ('BB-11-CC', 'Tesla',      'Model 3',    'ELETRICO',  2021, 2,  22000.00),
    ('CC-22-DD', 'Volkswagen', 'Passat',     'COMBUSTAO', 2018, 1,  78000.50),
    ('DD-33-EE', 'Mercedes',   'E-Class',    'COMBUSTAO', 2022, 2,   8000.00),
    ('EE-44-FF', 'Hyundai',    'Ioniq 6',   'ELETRICO',  2023, 2,   3500.75);


-- TURNO
INSERT INTO turno (motorista_id, taxi_id, inicio, fim) VALUES
    (6,  1, '2025-06-01 08:00:00', '2025-06-01 16:00:00'),
    (8,  2, '2025-06-01 09:00:00', '2025-06-01 17:00:00'),
    (10, 3, '2025-06-01 14:00:00', '2025-06-01 22:00:00'),
    (6,  4, '2025-06-02 08:00:00', '2025-06-02 16:00:00'),
    (8,  5, '2025-06-02 10:00:00', '2025-06-02 18:00:00');

-- INSERT INTO turno (motorista_id, taxi_id, inicio, fim) VALUES
--     (18,  3, '2026-05-02 10:00:00', '2026-05-02 18:00:00'),
--     (18,  4, '2026-05-03 10:00:00', '2026-05-03 18:00:00'),
--     (19,  1, '2026-05-02 10:00:00', '2026-05-02 18:00:00'),
--     (19,  2, '2026-05-03 10:00:00', '2026-05-03 18:00:00');


-- PEDIDO
INSERT INTO pedido (cliente_id, conforto_id, motorista_id, num_passageiros, estado_pedido,
                    rua_origem, latitude_origem, longitude_origem,
                    rua_destino, latitude_destino, longitude_destino) VALUES
    (1, 1, 6,  2,  'CONCLUIDO', 'Rua Augusta',          38.710000, -9.137000, 'Avenida Almirante Reis', 38.720000, -9.134000),
    (3, 2, 8,  1, 'CONCLUIDO', 'Praça do Comércio',    38.707000, -9.136000, 'Parque das Nações',      38.768000, -9.098000),
    (5, 1, 11, 1, 'PENDENTE',  'Intendente',           38.722000, -9.133000, 'Campo de Ourique',       38.714000, -9.158000),
    (7, 2, 10, 3, 'CONCLUIDO', 'Belém',                38.697000, -9.205000, 'Aeroporto de Lisboa',    38.774000, -9.134000),
    (9, 1, 6,  2, 'CANCELADO', 'Mouraria',             38.716000, -9.132000, 'Alfama',                 38.712000, -9.128000);

-- INSERT INTO pedido (cliente_id, conforto_id, motorista_id, num_passageiros, estado_pedido,
--                     rua_origem, latitude_origem, longitude_origem,
--                     rua_destino, latitude_destino, longitude_destino) VALUES
--     (16, 1, 18,  2,  'CONCLUIDO', 'Rua Augusta',          38.710000, -9.137000, 'Avenida Almirante Reis', 38.720000, -9.134000),
--     (17, 2, 18,  2,  'CONCLUIDO', 'Rua Augusta',          38.710000, -9.137000, 'Avenida Almirante Reis', 38.720000, -9.134000),
--     (16, 2, 19,  1, 'CONCLUIDO', 'Praça do Comércio',    38.707000, -9.136000, 'Parque das Nações',      38.768000, -9.098000),
--     (17, 1, 19,  1, 'CONCLUIDO', 'Praça do Comércio',    38.707000, -9.136000, 'Parque das Nações',      38.768000, -9.098000);


-- VIAGEM
INSERT INTO viagem (cliente_id, turno_id, pedido_id, inicio, fim, km_percorridos, preco) VALUES
    (1, 1, 1, '2025-06-01 08:30:00', '2025-06-01 08:52:00', 4.20, 201.50),
    (3, 2, 2, '2025-06-01 09:15:00', '2025-06-01 09:55:00', 11.80, 150.00),
    (7, 3, 4, '2025-06-01 15:00:00', '2025-06-01 15:45:00', 18.30, 250.00);

-- INSERT INTO viagem (cliente_id, turno_id, pedido_id, inicio, fim, km_percorridos, preco) VALUES
--     (17, 11, 11, '2026-05-02 10:30:00', '2026-05-02 10:52:00', 4.20, 21.50),
--     (17, 12, 13, '2026-05-03 13:15:00', '2026-05-03 13:31:00', 11.80, 15.00),
--     (16, 13, 10, '2026-05-02 15:00:00', '2026-05-02 15:22:00', 14.30, 25.00),
--     (16, 14, 12, '2026-05-03 16:02:00', '2026-05-03 16:35:00', 18.40, 32.00);


-- FATURA
INSERT INTO fatura (viagem_id, num_sequencial, data) VALUES
    (1, 1001, '2025-06-01 08:53:00'),
    (2, 1002, '2025-06-01 09:56:00'),
    (3, 1003, '2025-06-01 15:46:00');

-- INSERT INTO fatura (viagem_id, num_sequencial, data) VALUES
--     (8, 1008, '2026-05-02 10:53:00'),
--     (9, 1009, '2026-05-03 13:56:00'),
--     (10, 1010, '2026-05-02 15:46:00'),
--     (11, 1011, '2026-05-03 16:46:00');


-- REABASTECIMENTO
INSERT INTO reabastecimento (turno_id, data_inicio, data_fim, quantidade, euros_pagos, kilometros) VALUES
    (1, '2025-06-01 12:00:00', '2025-06-01 12:15:00', 40.00, 62.00, 320.00),
    (2, '2025-06-01 13:00:00', '2025-06-01 13:10:00', 20.00, 28.00, 180.00),
    (3, '2025-06-01 18:30:00', '2025-06-01 18:45:00', 35.00, 54.25, 270.00),
    (4, '2025-06-02 12:30:00', '2025-06-02 12:40:00', 30.00, 46.50, 220.00),
    (5, '2025-06-02 14:00:00', '2025-06-02 14:12:00', 25.00, 35.00, 160.00);

INSERT INTO configuracao_taxas (chave, valor) 
VALUES ('TAXA_NOTURNA', 1.30);