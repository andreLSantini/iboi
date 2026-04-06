CREATE TABLE receitas (
    id UUID PRIMARY KEY,
    farm_id UUID NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    tipo VARCHAR(30) NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    data DATE NOT NULL,
    forma_pagamento VARCHAR(30) NOT NULL,
    lote_id UUID REFERENCES lotes(id) ON DELETE SET NULL,
    animal_id UUID REFERENCES animais(id) ON DELETE SET NULL,
    responsavel_id UUID REFERENCES usuarios(id) ON DELETE SET NULL,
    comprador VARCHAR(120),
    quantidade_animais INTEGER,
    observacoes VARCHAR(1000),
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_receita_farm ON receitas(farm_id);
CREATE INDEX idx_receita_data ON receitas(data);
CREATE INDEX idx_receita_tipo ON receitas(tipo);
