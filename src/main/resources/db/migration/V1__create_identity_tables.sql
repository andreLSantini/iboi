CREATE TABLE empresas (
    id UUID PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    cnpj VARCHAR(18) UNIQUE,
    empresa_matriz_id UUID,
    asaas_customer_id VARCHAR(120),
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    criada_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE farms (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(2) NOT NULL,
    production_type VARCHAR(20) NOT NULL,
    size DOUBLE PRECISION,
    owner_name VARCHAR(150),
    owner_document VARCHAR(40),
    phone VARCHAR(30),
    email VARCHAR(150),
    address_line VARCHAR(255),
    zip_code VARCHAR(20),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    legal_status VARCHAR(60),
    document_proof VARCHAR(255),
    ccir VARCHAR(60),
    cib VARCHAR(60),
    car VARCHAR(60),
    main_exploration VARCHAR(120),
    estimated_capacity INT,
    grazing_area DOUBLE PRECISION,
    legal_reserve_area DOUBLE PRECISION,
    app_area DOUBLE PRECISION,
    productive_area DOUBLE PRECISION,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    CONSTRAINT uk_farms_name_empresa UNIQUE (name, empresa_id)
);

CREATE INDEX idx_farms_empresa ON farms(empresa_id);

CREATE TABLE usuarios (
    id UUID PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    telefone VARCHAR(20),
    senha_hash VARCHAR(255) NOT NULL,
    role_enum VARCHAR(20) NOT NULL,
    empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_empresa ON usuarios(empresa_id);

CREATE TABLE profiles (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE roles (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE permissions (
    id UUID PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL
);

CREATE TABLE role_permissions (
    id UUID PRIMARY KEY,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    CONSTRAINT uk_role_permissions UNIQUE (role_id, permission_id)
);

CREATE TABLE profile_permissions (
    profile_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (profile_id, permission_id)
);

CREATE TABLE user_farm_profiles (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    farm_id UUID NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_user_farm_profiles UNIQUE (usuario_id, farm_id)
);

CREATE INDEX idx_user_farm_profiles_usuario ON user_farm_profiles(usuario_id);
CREATE INDEX idx_user_farm_profiles_farm ON user_farm_profiles(farm_id);

CREATE TABLE farm_modules (
    id UUID PRIMARY KEY,
    farm_id UUID NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    module_code VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_farm_modules UNIQUE (farm_id, module_code)
);
