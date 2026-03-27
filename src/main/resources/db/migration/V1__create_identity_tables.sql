-- =====================================================
-- MIGRATION V1: Identity Module (Autenticação)
-- =====================================================

-- Tabela: Empresa (Tenant principal)
CREATE TABLE empresa (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(200) NOT NULL,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('MATRIZ', 'FILIAL')),
    cnpj VARCHAR(18),
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabela: Farm (Fazenda - contexto de negócio)
CREATE TABLE farm (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(2) NOT NULL,
    production_type VARCHAR(20) NOT NULL CHECK (production_type IN ('CORTE', 'LEITE', 'MISTO')),
    size DECIMAL(10,2),
    empresa_id UUID NOT NULL REFERENCES empresa(id) ON DELETE CASCADE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_farm_name_empresa UNIQUE (name, empresa_id)
);

CREATE INDEX idx_farm_empresa ON farm(empresa_id);

-- Tabela: Usuario
CREATE TABLE usuario (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    telefone VARCHAR(20),
    senha_hash VARCHAR(255) NOT NULL,
    role_enum VARCHAR(20) NOT NULL CHECK (role_enum IN ('ADMIN', 'USER')),
    empresa_id UUID NOT NULL REFERENCES empresa(id) ON DELETE CASCADE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_usuario_email ON usuario(email);
CREATE INDEX idx_usuario_empresa ON usuario(empresa_id);

-- Tabela: Profile (Perfil básico)
CREATE TABLE profile (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    bio TEXT,
    avatar_url VARCHAR(500),
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_profile_usuario UNIQUE (usuario_id)
);

-- Tabela: Role (Papel no sistema)
CREATE TABLE role (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT
);

-- Tabela: Permission (Permissões)
CREATE TABLE permission (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT
);

-- Tabela: RolePermission (Relação Role <-> Permission)
CREATE TABLE role_permission (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id UUID NOT NULL REFERENCES role(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permission(id) ON DELETE CASCADE,
    CONSTRAINT uk_role_permission UNIQUE (role_id, permission_id)
);

-- Tabela: FarmRole (Papel dentro de uma fazenda)
CREATE TABLE farm_role (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL,
    description TEXT
);

-- Inserir FarmRoles padrão
INSERT INTO farm_role (id, name, description) VALUES
('a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', 'ADMIN', 'Administrador da fazenda'),
('b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2', 'MANAGER', 'Gerente da fazenda'),
('c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3', 'OPERATOR', 'Operador da fazenda'),
('d4d4d4d4-d4d4-d4d4-d4d4-d4d4d4d4d4d4', 'VIEWER', 'Visualizador');

-- Tabela: UserFarmProfile (Usuário + Farm + Role)
CREATE TABLE user_farm_profile (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    farm_id UUID NOT NULL REFERENCES farm(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'MANAGER', 'OPERATOR', 'VIEWER')),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_farm UNIQUE (usuario_id, farm_id)
);

CREATE INDEX idx_ufp_usuario ON user_farm_profile(usuario_id);
CREATE INDEX idx_ufp_farm ON user_farm_profile(farm_id);

-- Tabela: FarmModule (Módulos habilitados por fazenda)
CREATE TABLE farm_module (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id UUID NOT NULL REFERENCES farm(id) ON DELETE CASCADE,
    module_name VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_farm_module UNIQUE (farm_id, module_name)
);
