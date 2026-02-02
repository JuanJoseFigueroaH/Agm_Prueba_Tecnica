CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE clientes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nombre VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    telefono VARCHAR(15),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX idx_clientes_email ON clientes(LOWER(email)) WHERE deleted_at IS NULL;
CREATE INDEX idx_clientes_activo ON clientes(activo);
CREATE INDEX idx_clientes_deleted_at ON clientes(deleted_at);
CREATE INDEX idx_clientes_created_at ON clientes(created_at DESC);
