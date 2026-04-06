ALTER TABLE despesas
    ADD COLUMN data_vencimento DATE,
    ADD COLUMN data_liquidacao DATE,
    ADD COLUMN status VARCHAR(30);

UPDATE despesas
SET data_vencimento = data,
    data_liquidacao = data,
    status = 'PAGO'
WHERE data_vencimento IS NULL;

ALTER TABLE despesas
    ALTER COLUMN data_vencimento SET NOT NULL,
    ALTER COLUMN status SET NOT NULL;

CREATE INDEX idx_despesa_data_vencimento ON despesas (data_vencimento);
CREATE INDEX idx_despesa_status ON despesas (status);

ALTER TABLE receitas
    ADD COLUMN data_vencimento DATE,
    ADD COLUMN data_liquidacao DATE,
    ADD COLUMN status VARCHAR(30);

UPDATE receitas
SET data_vencimento = data,
    data_liquidacao = data,
    status = 'RECEBIDO'
WHERE data_vencimento IS NULL;

ALTER TABLE receitas
    ALTER COLUMN data_vencimento SET NOT NULL,
    ALTER COLUMN status SET NOT NULL;

CREATE INDEX idx_receita_data_vencimento ON receitas (data_vencimento);
CREATE INDEX idx_receita_status ON receitas (status);
