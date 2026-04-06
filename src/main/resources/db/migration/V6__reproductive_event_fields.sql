ALTER TABLE eventos ADD COLUMN IF NOT EXISTS reprodutor_nome VARCHAR(120);
ALTER TABLE eventos ADD COLUMN IF NOT EXISTS protocolo_reprodutivo VARCHAR(120);
ALTER TABLE eventos ADD COLUMN IF NOT EXISTS diagnostico_positivo BOOLEAN;
ALTER TABLE eventos ADD COLUMN IF NOT EXISTS data_prevista_parto DATE;
ALTER TABLE eventos ADD COLUMN IF NOT EXISTS observacao_reprodutiva VARCHAR(500);

CREATE INDEX IF NOT EXISTS idx_evento_data_prevista_parto ON eventos(data_prevista_parto);
