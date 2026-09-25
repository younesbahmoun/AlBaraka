CREATE TABLE clients (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE comptes (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero VARCHAR(50) NOT NULL UNIQUE,
    solde NUMERIC(19, 2) NOT NULL,
    id_client BIGINT NOT NULL REFERENCES clients(id),
    type_compte VARCHAR(20) NOT NULL
        CHECK (type_compte IN ('COURANT', 'EPARGNE')),
    decouvert_autorise NUMERIC(19, 2),
    taux_interet NUMERIC(10, 4),

    CONSTRAINT parametres_type_compte CHECK (
        (
            type_compte = 'COURANT'
            AND decouvert_autorise IS NOT NULL
            AND decouvert_autorise >= 0
            AND taux_interet IS NULL
            AND solde >= -decouvert_autorise
        )
        OR
        (
            type_compte = 'EPARGNE'
            AND taux_interet IS NOT NULL
            AND taux_interet >= 0
            AND decouvert_autorise IS NULL
            AND solde >= 0
        )
    )
);

CREATE TABLE transactions (
    id SERIAL PRIMARY KEY,
    date_transaction TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    montant NUMERIC(19, 2) NOT NULL CHECK (montant > 0),
    type_transaction VARCHAR(20) NOT NULL CHECK (type_transaction IN ('VERSEMENT', 'RETRAIT', 'VIREMENT')),
    lieu VARCHAR(150) NOT NULL,
    id_compte BIGINT NOT NULL REFERENCES comptes(id),
    id_compte_destination BIGINT REFERENCES comptes(id),

    CONSTRAINT destination_virement CHECK (
        (
            type_transaction = 'VIREMENT'
            AND id_compte_destination IS NOT NULL
            AND id_compte_destination <> id_compte
        )
        OR
        (
            type_transaction <> 'VIREMENT'
            AND id_compte_destination IS NULL
        )
    )
);

CREATE INDEX idx_comptes_client ON comptes(id_client);
CREATE INDEX idx_transactions_compte_date ON transactions(id_compte, date_transaction);
CREATE INDEX idx_transactions_destination ON transactions(id_compte_destination);


-- 1. CLIENTS
INSERT INTO clients (nom, email) VALUES
('Younes Bahmoun', 'younes.test@example.com'),
('Sara Amine', 'sara.test@example.com'),
('Adam Karim', 'adam.test@example.com'),
('Salma Idrissi', 'salma.test@example.com'),
('Omar Alaoui', 'omar.test@example.com'),
('Imane Bennani', 'imane.test@example.com'),
('Mehdi Fassi', 'mehdi.test@example.com'),
('Lina Mansouri', 'lina.test@example.com'),
('Ayoub El Amrani', 'ayoub.test@example.com'),
('Nadia Rami', 'nadia.test@example.com');



-- 2. COMPTES
INSERT INTO comptes
    (numero, solde, id_client, type_compte, decouvert_autorise, taux_interet)
VALUES
('TEST-C001', 12100.00,
 (SELECT id FROM clients WHERE email = 'younes.test@example.com'),
 'COURANT', 500.00, NULL),

('TEST-E002', 850.00,
 (SELECT id FROM clients WHERE email = 'sara.test@example.com'),
 'EPARGNE', NULL, 0.0250),

('TEST-C003', 1300.00,
 (SELECT id FROM clients WHERE email = 'adam.test@example.com'),
 'COURANT', 300.00, NULL),

('TEST-E004', 900.00,
 (SELECT id FROM clients WHERE email = 'salma.test@example.com'),
 'EPARGNE', NULL, 0.0300),

('TEST-C005', 2000.00,
 (SELECT id FROM clients WHERE email = 'omar.test@example.com'),
 'COURANT', 400.00, NULL),

('TEST-E006', 300.00,
 (SELECT id FROM clients WHERE email = 'imane.test@example.com'),
 'EPARGNE', NULL, 0.0200),

('TEST-C007', 0.00,
 (SELECT id FROM clients WHERE email = 'mehdi.test@example.com'),
 'COURANT', 200.00, NULL),

('TEST-E008', 0.00,
 (SELECT id FROM clients WHERE email = 'lina.test@example.com'),
 'EPARGNE', NULL, 0.0150),

('TEST-C009', 0.00,
 (SELECT id FROM clients WHERE email = 'ayoub.test@example.com'),
 'COURANT', 100.00, NULL),

('TEST-E010', 0.00,
 (SELECT id FROM clients WHERE email = 'nadia.test@example.com'),
 'EPARGNE', NULL, 0.0200);






 -- 3. TRANSACTIONS
INSERT INTO transactions
    (date_transaction, montant, type_transaction, lieu,
     id_compte, id_compte_destination)
VALUES
-- Trois opérations du même client en moins d'une minute
('2026-09-01 10:00:00', 1200.00, 'VERSEMENT', 'Salé',
 (SELECT id FROM comptes WHERE numero = 'TEST-C001'), NULL),

('2026-07-10 09:00:00', 900.00, 'VERSEMENT', 'Salé',
 (SELECT id FROM comptes WHERE numero = 'TEST-E002'), NULL),

('2026-08-05 11:30:00', 1500.00, 'VERSEMENT', 'Rabat',
 (SELECT id FROM comptes WHERE numero = 'TEST-C003'), NULL),

('2026-08-06 14:00:00', 700.00, 'VERSEMENT', 'Salé',
 (SELECT id FROM comptes WHERE numero = 'TEST-E004'), NULL),

('2026-09-03 08:15:00', 2000.00, 'VERSEMENT', 'Casablanca',
 (SELECT id FROM comptes WHERE numero = 'TEST-C005'), NULL),

('2026-09-01 10:00:20', 100.00, 'RETRAIT', 'Salé',
 (SELECT id FROM comptes WHERE numero = 'TEST-C001'), NULL),

('2026-08-07 12:00:00', 200.00, 'VIREMENT', 'Rabat',
 (SELECT id FROM comptes WHERE numero = 'TEST-C003'),
 (SELECT id FROM comptes WHERE numero = 'TEST-E004')),

('2026-09-04 16:45:00', 300.00, 'VERSEMENT', 'Salé',
 (SELECT id FROM comptes WHERE numero = 'TEST-E006'), NULL),

('2026-07-12 10:20:00', 50.00, 'RETRAIT', 'Salé',
 (SELECT id FROM comptes WHERE numero = 'TEST-E002'), NULL),

-- Montant élevé + troisième opération en moins d'une minute
('2026-09-01 10:00:40', 11000.00, 'VERSEMENT', 'Marrakech',
 (SELECT id FROM comptes WHERE numero = 'TEST-C001'), NULL);