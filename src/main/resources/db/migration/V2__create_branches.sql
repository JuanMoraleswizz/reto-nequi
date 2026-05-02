-- V2: Tabla de sucursales
-- Cada sucursal pertenece a una franquicia (relación 1:N)
CREATE TABLE IF NOT EXISTS branches (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    franchise_id UUID         NOT NULL REFERENCES franchises(id) ON DELETE CASCADE,
    name         VARCHAR(255) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Índice para acelerar búsquedas de sucursales por franquicia
CREATE INDEX IF NOT EXISTS idx_branches_franchise_id ON branches(franchise_id);
