CREATE TABLE lotes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(500),
    farm_id UUID NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_lotes_nome_farm UNIQUE (nome, farm_id)
);

CREATE INDEX idx_lotes_farm ON lotes(farm_id);
CREATE INDEX idx_lotes_ativo ON lotes(ativo);

CREATE TABLE animais (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    brinco VARCHAR(50) NOT NULL,
    nome VARCHAR(100),
    sexo VARCHAR(10) NOT NULL,
    raca VARCHAR(50) NOT NULL,
    data_nascimento DATE NOT NULL,
    peso_atual DECIMAL(10,2),
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    categoria VARCHAR(20) NOT NULL,
    farm_id UUID NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    lote_id UUID REFERENCES lotes(id) ON DELETE SET NULL,
    pai_id UUID REFERENCES animais(id) ON DELETE SET NULL,
    mae_id UUID REFERENCES animais(id) ON DELETE SET NULL,
    observacoes VARCHAR(1000),
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP,
    CONSTRAINT uk_animais_brinco_farm UNIQUE (brinco, farm_id)
);

CREATE INDEX idx_animal_farm ON animais(farm_id);
CREATE INDEX idx_animal_brinco ON animais(brinco);
CREATE INDEX idx_animal_status ON animais(status);
CREATE INDEX idx_animal_lote ON animais(lote_id);

CREATE TABLE eventos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    animal_id UUID NOT NULL REFERENCES animais(id) ON DELETE CASCADE,
    farm_id UUID NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    tipo VARCHAR(30) NOT NULL,
    data DATE NOT NULL,
    descricao VARCHAR(1000) NOT NULL,
    peso DECIMAL(10,2),
    produto VARCHAR(200),
    dose DECIMAL(10,2),
    unidade_medida VARCHAR(50),
    lote_destino_id UUID REFERENCES lotes(id) ON DELETE SET NULL,
    valor DECIMAL(10,2),
    responsavel_id UUID REFERENCES usuarios(id) ON DELETE SET NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_evento_animal ON eventos(animal_id);
CREATE INDEX idx_evento_farm ON eventos(farm_id);
CREATE INDEX idx_evento_tipo ON eventos(tipo);
CREATE INDEX idx_evento_data ON eventos(data);
