CREATE TABLE despesas (
    id UUID PRIMARY KEY,
    farm_id UUID NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    categoria VARCHAR(30) NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    data DATE NOT NULL,
    data_vencimento DATE NOT NULL,
    data_liquidacao DATE,
    forma_pagamento VARCHAR(40) NOT NULL,
    status VARCHAR(30) NOT NULL,
    lote_id UUID REFERENCES lotes(id) ON DELETE SET NULL,
    animal_id UUID REFERENCES animais(id) ON DELETE SET NULL,
    responsavel_id UUID REFERENCES usuarios(id) ON DELETE SET NULL,
    observacoes VARCHAR(1000),
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_despesa_farm ON despesas (farm_id);
CREATE INDEX idx_despesa_data ON despesas (data);
CREATE INDEX idx_despesa_categoria ON despesas (categoria);
CREATE INDEX idx_despesa_data_vencimento ON despesas (data_vencimento);
CREATE INDEX idx_despesa_status ON despesas (status);

ALTER TABLE receitas ADD COLUMN data_vencimento DATE;
ALTER TABLE receitas ADD COLUMN data_liquidacao DATE;
ALTER TABLE receitas ADD COLUMN status VARCHAR(30);

UPDATE receitas
SET data_vencimento = data,
    data_liquidacao = data,
    status = 'RECEBIDO'
WHERE data_vencimento IS NULL;

ALTER TABLE receitas ALTER COLUMN data_vencimento SET NOT NULL;
ALTER TABLE receitas ALTER COLUMN status SET NOT NULL;

CREATE INDEX idx_receita_data_vencimento ON receitas (data_vencimento);
CREATE INDEX idx_receita_status ON receitas (status);
