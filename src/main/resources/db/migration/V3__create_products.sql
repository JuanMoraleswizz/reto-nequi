-- V3: Tabla de productos
-- Cada producto pertenece a una sucursal (relación 1:N)
CREATE TABLE IF NOT EXISTS products (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id  UUID         NOT NULL REFERENCES branches(id) ON DELETE CASCADE,
    name       VARCHAR(255) NOT NULL,
    stock      INTEGER      NOT NULL CHECK (stock >= 0),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Índice para búsquedas de productos por sucursal
CREATE INDEX IF NOT EXISTS idx_products_branch_id ON products(branch_id);

-- Índice compuesto para la query DISTINCT ON de max-stock (branch_id + stock DESC)
CREATE INDEX IF NOT EXISTS idx_products_branch_stock ON products(branch_id, stock DESC);
