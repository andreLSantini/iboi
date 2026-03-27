CREATE TABLE IF NOT EXISTS pastures (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    area_ha DOUBLE PRECISION,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    geo_json VARCHAR(4000),
    notes VARCHAR(1000),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    farm_id UUID NOT NULL REFERENCES farms(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_pastures_farm ON pastures(farm_id);

ALTER TABLE animais ADD COLUMN IF NOT EXISTS rfid VARCHAR(64);
ALTER TABLE animais ADD COLUMN IF NOT EXISTS codigo_sisbov VARCHAR(64);
ALTER TABLE animais ADD COLUMN IF NOT EXISTS pasture_id UUID;
ALTER TABLE animais ADD COLUMN IF NOT EXISTS data_entrada DATE;
ALTER TABLE animais ADD COLUMN IF NOT EXISTS sisbov_ativo BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_animal_rfid ON animais(rfid);
CREATE INDEX IF NOT EXISTS idx_animal_sisbov ON animais(codigo_sisbov);
CREATE INDEX IF NOT EXISTS idx_animal_pasture ON animais(pasture_id);

ALTER TABLE animais
    DROP CONSTRAINT IF EXISTS fk_animais_pasture;

ALTER TABLE animais
    ADD CONSTRAINT fk_animais_pasture
    FOREIGN KEY (pasture_id) REFERENCES pastures(id);

CREATE TABLE IF NOT EXISTS movimentacoes_animais (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    animal_id UUID NOT NULL REFERENCES animais(id) ON DELETE CASCADE,
    tipo VARCHAR(40) NOT NULL,
    farm_origem_id UUID REFERENCES farms(id),
    farm_destino_id UUID REFERENCES farms(id),
    pasture_origem_id UUID REFERENCES pastures(id),
    pasture_destino_id UUID REFERENCES pastures(id),
    movimentada_em DATE NOT NULL,
    numero_gta VARCHAR(64),
    documento_externo VARCHAR(128),
    motivo VARCHAR(255),
    observacoes VARCHAR(1000),
    responsavel_id UUID REFERENCES usuarios(id),
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_movimentacao_animal ON movimentacoes_animais(animal_id);
CREATE INDEX IF NOT EXISTS idx_movimentacao_farm ON movimentacoes_animais(farm_origem_id, farm_destino_id);
CREATE INDEX IF NOT EXISTS idx_movimentacao_data ON movimentacoes_animais(movimentada_em);

CREATE TABLE IF NOT EXISTS vacinacoes_animais (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    animal_id UUID NOT NULL REFERENCES animais(id) ON DELETE CASCADE,
    farm_id UUID NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    tipo VARCHAR(40) NOT NULL,
    nome_vacina VARCHAR(120) NOT NULL,
    dose NUMERIC(10,2),
    unidade_medida VARCHAR(30),
    aplicada_em DATE NOT NULL,
    proxima_dose_em DATE,
    fabricante VARCHAR(120),
    lote_vacina VARCHAR(80),
    observacoes VARCHAR(1000),
    responsavel_id UUID REFERENCES usuarios(id),
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_vacinacao_animal ON vacinacoes_animais(animal_id);
CREATE INDEX IF NOT EXISTS idx_vacinacao_farm ON vacinacoes_animais(farm_id);
CREATE INDEX IF NOT EXISTS idx_vacinacao_data ON vacinacoes_animais(aplicada_em);
CREATE INDEX IF NOT EXISTS idx_vacinacao_tipo ON vacinacoes_animais(tipo);
