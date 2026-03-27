CREATE TABLE assinaturas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL UNIQUE REFERENCES empresas(id) ON DELETE CASCADE,
    tipo VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    periodo_pagamento VARCHAR(20),
    data_inicio TIMESTAMP NOT NULL,
    data_vencimento TIMESTAMP NOT NULL,
    proxima_cobranca TIMESTAMP,
    valor DECIMAL(10,2),
    criada_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_assinaturas_empresa ON assinaturas(empresa_id);
CREATE INDEX idx_assinaturas_status ON assinaturas(status);

CREATE TABLE plano_preco (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL,
    tipo VARCHAR(20) NOT NULL UNIQUE,
    valor_mensal DECIMAL(10,2) NOT NULL,
    descricao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO plano_preco (nome, tipo, valor_mensal, descricao, ativo) VALUES
('Trial', 'TRIAL', 0.00, 'Acesso temporario completo para conhecer a plataforma.', TRUE),
('Free', 'FREE', 0.00, 'Plano de entrada com limite de ate 50 animais.', TRUE),
('Basic', 'BASIC', 79.00, 'Cadastro completo, pesagem, vacinacao e manejo operacional.', TRUE),
('Pro', 'PRO', 199.00, 'Relatorios e leitura economica da fazenda.', TRUE),
('Premium', 'PREMIUM', 399.00, 'Camada decisoria com IA, predicao e recomendacoes.', TRUE),
('Enterprise', 'ENTERPRISE', 799.00, 'Conta corporativa e consultiva para operacoes maiores.', TRUE)
ON CONFLICT (tipo) DO NOTHING;

CREATE TABLE pagamentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assinatura_id UUID NOT NULL REFERENCES assinaturas(id) ON DELETE CASCADE,
    valor DECIMAL(10,2) NOT NULL,
    data_vencimento TIMESTAMP NOT NULL,
    data_pagamento TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    metodo_pagamento VARCHAR(40),
    transacao_id VARCHAR(255),
    gateway_provider VARCHAR(80),
    invoice_url VARCHAR(500),
    bank_slip_url VARCHAR(500),
    pix_payload VARCHAR(4000),
    pix_encoded_image TEXT,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pagamentos_assinatura ON pagamentos(assinatura_id);
CREATE INDEX idx_pagamentos_status ON pagamentos(status);
