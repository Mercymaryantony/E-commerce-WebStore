-- =====================================================
-- Flyway Migration V6: Add Seller, Image URL, and Stock to Products
-- =====================================================

-- Add seller_id column to products table
ALTER TABLE web_store.products 
ADD COLUMN IF NOT EXISTS seller_id INT;

-- Update any existing NULL seller_id values - assign first available seller from sellers table
-- This ensures all existing products have a seller_id before making it NOT NULL
DO $$ 
DECLARE
    first_seller_id INT;
    products_updated INT;
BEGIN
    -- Get the first seller ID from sellers table
    SELECT seller_id INTO first_seller_id 
    FROM web_store.sellers 
    ORDER BY seller_id ASC 
    LIMIT 1;
    
    -- If sellers exist, assign the first seller to all products with NULL seller_id
    IF first_seller_id IS NOT NULL THEN
        -- Update all products that have NULL seller_id
        UPDATE web_store.products 
        SET seller_id = first_seller_id 
        WHERE seller_id IS NULL;
        
        GET DIAGNOSTICS products_updated = ROW_COUNT;
        
        RAISE NOTICE 'Assigned seller_id % to % existing products', first_seller_id, products_updated;
    ELSE
        RAISE EXCEPTION 'No sellers found in sellers table. Please create at least one seller before running this migration.';
    END IF;
END $$;

-- Add foreign key constraint for seller_id
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'fk_product_seller'
    ) THEN
        ALTER TABLE web_store.products
        ADD CONSTRAINT fk_product_seller 
            FOREIGN KEY (seller_id) 
            REFERENCES web_store.sellers(seller_id) 
            ON DELETE RESTRICT;
    END IF;
END $$;

-- Make seller_id NOT NULL after ensuring no null values exist
DO $$
BEGIN
    -- Only set NOT NULL if there are no NULL values
    IF NOT EXISTS (SELECT 1 FROM web_store.products WHERE seller_id IS NULL) THEN
        ALTER TABLE web_store.products
        ALTER COLUMN seller_id SET NOT NULL;
    ELSE
        RAISE WARNING 'Cannot set seller_id to NOT NULL: there are still products with NULL seller_id';
    END IF;
END $$;

-- Add image_url column
ALTER TABLE web_store.products 
ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);

-- Add stock column
ALTER TABLE web_store.products 
ADD COLUMN IF NOT EXISTS stock INTEGER DEFAULT 0;

-- Create index on seller_id for better query performance
CREATE INDEX IF NOT EXISTS idx_product_seller_id ON web_store.products(seller_id);

-- Add comment for documentation
COMMENT ON COLUMN web_store.products.seller_id IS 'Foreign key to sellers table. Links product to seller.';
COMMENT ON COLUMN web_store.products.image_url IS 'URL to product image for display in admin dashboard';
COMMENT ON COLUMN web_store.products.stock IS 'Number of items available in stock';