-- V3__Update_Product_CatalogueCategory_Relationship.sql
-- Changes Product relationship from Category to CatalogueCategory
-- Note: This migration uses SINGULAR table names (product, category, catalogue_category)
-- V4 migration will rename them to plural form

-- Step 1: Add new column (nullable first to allow existing data)
ALTER TABLE web_store.product 
ADD COLUMN IF NOT EXISTS catalogue_category_id INT;

-- Step 2: Migrate existing data (only if category_id column still exists)
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'web_store' 
        AND table_name = 'product' 
        AND column_name = 'category_id'
    ) THEN
        -- Migrate existing data
        UPDATE web_store.product p
        SET catalogue_category_id = (
            SELECT cc.catalogue_category_id 
            FROM web_store.catalogue_category cc
            WHERE cc.category_id = p.category_id
            AND cc.catalogue_id = 1
            LIMIT 1
        );
        
        -- Create missing catalogue_category entries
        INSERT INTO web_store.catalogue_category (catalogue_id, category_id, created_by, updated_by)
        SELECT DISTINCT 1, p.category_id, 'system', 'system'
        FROM web_store.product p
        WHERE p.catalogue_category_id IS NULL
        AND NOT EXISTS (
            SELECT 1 FROM web_store.catalogue_category cc 
            WHERE cc.category_id = p.category_id AND cc.catalogue_id = 1
        );
        
        -- Update products that still have NULL catalogue_category_id
        UPDATE web_store.product p
        SET catalogue_category_id = (
            SELECT cc.catalogue_category_id 
            FROM web_store.catalogue_category cc
            WHERE cc.category_id = p.category_id
            AND cc.catalogue_id = 1
            LIMIT 1
        )
        WHERE p.catalogue_category_id IS NULL;
    END IF;
END $$;

-- Step 3: Set default catalogue_category_id for any NULL values
-- (Safety check in case data migration was skipped)
UPDATE web_store.product 
SET catalogue_category_id = 1
WHERE catalogue_category_id IS NULL;

-- Step 4: Add foreign key constraint
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'fk_product_catalogue_category'
    ) THEN
        ALTER TABLE web_store.product
        ADD CONSTRAINT fk_product_catalogue_category 
            FOREIGN KEY (catalogue_category_id) 
            REFERENCES web_store.catalogue_category (catalogue_category_id);
    END IF;
END $$;

-- Step 6: Make column NOT NULL (after data migration)
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'web_store' 
        AND table_name = 'product' 
        AND column_name = 'catalogue_category_id'
        AND is_nullable = 'YES'
    ) THEN
        ALTER TABLE web_store.product 
        ALTER COLUMN catalogue_category_id SET NOT NULL;
    END IF;
END $$;

-- Step 7: Drop old foreign key constraint
ALTER TABLE web_store.product 
DROP CONSTRAINT IF EXISTS fk_product_category;

-- Step 8: Drop old category_id column
ALTER TABLE web_store.product 
DROP COLUMN IF EXISTS category_id;

-- Step 9: Create index for better query performance
CREATE INDEX IF NOT EXISTS idx_product_catalogue_category 
ON web_store.product(catalogue_category_id);

