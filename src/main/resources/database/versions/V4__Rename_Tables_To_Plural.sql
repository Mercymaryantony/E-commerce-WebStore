-- V4__Rename_Tables_To_Plural.sql
-- Renames all tables from singular to plural form for consistency

-- Rename tables (order matters due to foreign key dependencies)
ALTER TABLE IF EXISTS web_store.product_price RENAME TO product_prices;
ALTER TABLE IF EXISTS web_store.product RENAME TO products;
ALTER TABLE IF EXISTS web_store.catalogue_category RENAME TO catalogue_categories;
ALTER TABLE IF EXISTS web_store.category RENAME TO categories;
ALTER TABLE IF EXISTS web_store.catalogue RENAME TO catalogues;
ALTER TABLE IF EXISTS web_store.currency RENAME TO currencies;
-- Note: 'users' table is already plural, no change needed

-- Rename sequences for consistency
ALTER SEQUENCE IF EXISTS web_store.seq_product_id RENAME TO seq_products_id;
ALTER SEQUENCE IF EXISTS web_store.seq_product_price_id RENAME TO seq_product_prices_id;
ALTER SEQUENCE IF EXISTS web_store.seq_catalogue_id RENAME TO seq_catalogues_id;
ALTER SEQUENCE IF EXISTS web_store.seq_category_id RENAME TO seq_categories_id;
ALTER SEQUENCE IF EXISTS web_store.seq_catalogue_category_id RENAME TO seq_catalogue_categories_id;
ALTER SEQUENCE IF EXISTS web_store.seq_currency_id RENAME TO seq_currencies_id;
-- Note: seq_user_id remains unchanged

-- Rename indexes for consistency
ALTER INDEX IF EXISTS web_store.idx_product_catalogue_category RENAME TO idx_products_catalogue_category;

-- Update constraint names if needed (optional, but good practice)
-- Foreign key constraints are automatically updated when tables are renamed
-- Unique constraints are also automatically updated

-- Note: No data migration needed - RENAME is metadata-only operation

