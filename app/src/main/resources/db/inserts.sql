INSERT INTO conforto (nivel_conforto, preco) VALUES
 ('BASICO', 0.50),
 ('CONFORTO', 0.75),
 ('LUXUOSO', 1.0);


INSERT INTO utilizador (id, nome, email, nif, genero, senha, data_nascimento) VALUES
  (1, 'Gestor Master', 'gestor@teste.com', '123456789', 'MASCULINO', '$2a$12$wTEFcWtwil5moDXMRYhGpOsGuy3Bux2Md.KUidagdISF5BvoZ.lLu', '1990-01-01'),
  (2, 'Motorista Alpha', 'motorista@teste.com', '987654321', 'MASCULINO', '$2a$12$wTEFcWtwil5moDXMRYhGpOsGuy3Bux2Md.KUidagdISF5BvoZ.lLu', '1992-05-10'),
  (3, 'Cliente Beta', 'cliente@teste.com', '111222333', 'FEMININO', '$2a$12$wTEFcWtwil5moDXMRYhGpOsGuy3Bux2Md.KUidagdISF5BvoZ.lLu', '1995-09-20');


INSERT INTO gestor (id) VALUES (1);

INSERT INTO motorista (id, num_carta_conducao, validade_carta_conducao, rua, rua_numero, codigo_postal)
VALUES (2, 'PT-999999', '2030-12-31', 'Rua A', 10, '1000-100');

INSERT INTO cliente (id) VALUES (3);

INSERT INTO taxi (matricula, marca, modelo, tipo_motor, ano_compra, conforto_id, kilometros) VALUES
 ('AA-00-BB', 'Mercedes', 'E-Class', 'COMBUSTAO', 2020, 2, 50000.00),
 ('CC-11-DD', 'Tesla', 'Model 3', 'ELETRICO', 2022, 2, 12000.50),
 ('EE-22-FF', 'Dacia', 'Logan', 'COMBUSTAO', 2018, 1, 150000.00);


INSERT INTO turno (motorista_id, taxi_id, inicio, fim) VALUES
   (2, 1, '2026-04-11 08:00:00', '2026-04-11 16:00:00'),
   (2, 2, '2026-04-12 08:00:00', '2026-04-12 16:00:00'),
   (2, 3, '2026-04-13 22:00:00', '2026-04-14 06:00:00');

INSERT INTO configuracao_taxas (chave, valor) 
VALUES ('TAXA_NOTURNA', 1.30);