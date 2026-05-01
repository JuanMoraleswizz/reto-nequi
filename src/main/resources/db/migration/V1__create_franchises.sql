-- V1: Tabla de franquicias
-- Almacena las franquicias de la red Nequi
CREATE TABLE IF NOT EXISTS franchises (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
