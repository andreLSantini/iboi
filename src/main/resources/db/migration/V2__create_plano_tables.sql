-- =====================================================
-- MIGRATION V2: Plano/Assinatura Module
-- =====================================================

-- Tabela: Assinatura
CREATE TABLE assinatura (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL REFERENCES empresa(id) ON DELETE CASCADE,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('TRIAL', 'BASIC', 'PREMIUM', 'ENTERPRISE')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('TRIAL', 'ATIVA', 'SUSPENSA', 'CANCELADA', 'EXPIRADA')),
    data_inicio TIMESTAMP NOT NULL,
    data_vencimento TIMESTAMP NOT NULL,
    max_animais INT,
    max_fazendas INT,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_assinatura_empresa UNIQUE (empresa_id)
);

CREATE INDEX idx_assinatura_empresa ON assinatura(empresa_id);
CREATE INDEX idx_assinatura_status ON assinatura(status);

-- Tabela: PlanoPreco
CREATE TABLE plano_preco (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('TRIAL', 'BASIC', 'PREMIUM', 'ENTERPRISE')),
    valor_mensal DECIMAL(10,2) NOT NULL,
    max_animais INT,
    max_fazendas INT,
    descricao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Inserir planos padrão
INSERT INTO plano_preco (nome, tipo, valor_mensal, max_animais, max_fazendas, descricao, ativo) VALUES
('Trial Gratuito', 'TRIAL', 0.00, 50, 1, 'Período experimental de 30 dias', TRUE),
('Básico', 'BASIC', 49.90, 200, 2, 'Plano básico para pequenos produtores', TRUE),
('Premium', 'PREMIUM', 99.90, 1000, 5, 'Plano completo com IA e relatórios avançados', TRUE),
('Enterprise', 'ENTERPRISE', 299.90, NULL, NULL, 'Plano ilimitado para grandes fazendas', TRUE);

-- Tabela: Pagamento
CREATE TABLE pagamento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assinatura_id UUID NOT NULL REFERENCES assinatura(id) ON DELETE CASCADE,
    valor DECIMAL(10,2) NOT NULL,
    data_pagamento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDENTE', 'APROVADO', 'RECUSADO', 'CANCELADO')),
    metodo VARCHAR(50),
    transacao_id VARCHAR(255),
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pagamento_assinatura ON pagamento(assinatura_id);
CREATE INDEX idx_pagamento_status ON pagamento(status);
