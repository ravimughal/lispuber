INSERT INTO conforto (nivel_conforto, preco) VALUES 
('BASICO', 5.50),
('LUXUOSO', 15.00),
('BASICO', 7.00); -- Nota: a coluna nivel_conforto é UNIQUE, por isso isto daria erro. 
-- Vamos inserir apenas os dois tipos permitidos:


-- INSERT INTO conforto (nivel_conforto, preco) VALUES 
-- ('BASICO', 5.00),
-- ('LUXUOSO', 12.00);


INSERT INTO taxi (matricula, marca, modelo, tipo_motor, ano_compra, conforto_id, kilometros) VALUES 
('AA-00-BB', 'Mercedes', 'E-Class', 'COMBUSTAO', 2020, 2, 50000.00),
('CC-11-DD', 'Tesla', 'Model 3', 'ELETRICO', 2022, 2, 12000.50),
('EE-22-FF', 'Dacia', 'Logan', 'COMBUSTAO', 2018, 1, 150000.00);


INSERT INTO turno (motorista_id, taxi_id, inicio, fim) VALUES 
(2, 1, '2026-04-11 08:00:00', '2026-04-11 16:00:00'),
(2, 2, '2026-04-12 08:00:00', '2026-04-12 16:00:00'),
(2, 3, '2026-04-13 22:00:00', '2026-04-14 06:00:00');