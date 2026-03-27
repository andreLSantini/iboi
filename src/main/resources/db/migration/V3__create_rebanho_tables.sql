-- =====================================================
-- MIGRATION V3: Rebanho Module (Gestão de Gado)
-- =====================================================

-- Tabela: Lote
CREATE TABLE lote (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(500),
    farm_id UUID NOT NULL REFERENCES farm(id) ON DELETE CASCADE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_lote_nome_farm UNIQUE (nome, farm_id)
);

CREATE INDEX idx_lote_farm ON lote(farm_id);
CREATE INDEX idx_lote_ativo ON lote(ativo);

-- Tabela: Animal
CREATE TABLE animal (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    brinco VARCHAR(50) NOT NULL,
    nome VARCHAR(100),
    sexo VARCHAR(10) NOT NULL CHECK (sexo IN ('MACHO', 'FEMEA')),
    raca VARCHAR(50) NOT NULL CHECK (raca IN (
        'NELORE', 'ANGUS', 'BRAHMAN', 'CARACU', 'GUZERA', 'TABAPUA',
        'SENEPOL', 'CHAROLÊS', 'LIMOUSIN', 'HEREFORD', 'SIMENTAL',
        'CRUZAMENTO_INDUSTRIAL', 'MESTICO', 'OUTRAS'
    )),
    data_nascimento DATE NOT NULL,
    peso_atual DECIMAL(10,2),
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO' CHECK (status IN (
        'ATIVO', 'VENDIDO', 'MORTO', 'DESCARTADO', 'TRANSFERIDO'
    )),
    categoria VARCHAR(20) NOT NULL CHECK (categoria IN (
        'BEZERRO', 'NOVILHO', 'NOVILHA', 'BOI', 'VACA', 'TOURO', 'MATRIZ'
    )),
    farm_id UUID NOT NULL REFERENCES farm(id) ON DELETE CASCADE,
    lote_id UUID REFERENCES lote(id) ON DELETE SET NULL,
    pai_id UUID REFERENCES animal(id) ON DELETE SET NULL,
    mae_id UUID REFERENCES animal(id) ON DELETE SET NULL,
    observacoes TEXT,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_animal_brinco_farm UNIQUE (brinco, farm_id)
);

CREATE INDEX idx_animal_farm ON animal(farm_id);
CREATE INDEX idx_animal_brinco ON animal(brinco);
CREATE INDEX idx_animal_status ON animal(status);
CREATE INDEX idx_animal_lote ON animal(lote_id);

-- Tabela: Evento
CREATE TABLE evento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    animal_id UUID NOT NULL REFERENCES animal(id) ON DELETE CASCADE,
    farm_id UUID NOT NULL REFERENCES farm(id) ON DELETE CASCADE,
    tipo VARCHAR(30) NOT NULL CHECK (tipo IN (
        'VACINA', 'VERMIFUGO', 'PESAGEM', 'MOVIMENTACAO', 'NASCIMENTO', 'DESMAME',
        'MORTE', 'VENDA', 'COMPRA', 'TRATAMENTO', 'INSEMINACAO', 'COBERTURA',
        'PARTO', 'DIAGNOSTICO_GESTACAO', 'DESCARTE', 'OBSERVACAO'
    )),
    data DATE NOT NULL,
    descricao TEXT NOT NULL,
    peso DECIMAL(10,2),
    produto VARCHAR(200),
    dose DECIMAL(10,2),
    unidade_medida VARCHAR(20),
    lote_destino_id UUID REFERENCES lote(id) ON DELETE SET NULL,
    valor DECIMAL(10,2),
    responsavel_id UUID REFERENCES usuario(id) ON DELETE SET NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_evento_animal ON evento(animal_id);
CREATE INDEX idx_evento_farm ON evento(farm_id);
CREATE INDEX idx_evento_tipo ON evento(tipo);
CREATE INDEX idx_evento_data ON evento(data);
