-- =====================================================
-- Flyway Migration V7: Update Existing Products with Seller ID
-- =====================================================
-- This migration assigns seller_id to all existing products that have NULL seller_id
-- It uses the first available seller from the sellers table

-- Update any existing NULL seller_id values - assign first available seller from sellers table
DO $$ 
DECLARE
    first_seller_id INT;
    products_updated INT;
BEGIN
    -- Check if there are any products with NULL seller_id
    IF EXISTS (SELECT 1 FROM web_store.products WHERE seller_id IS NULL) THEN
        -- Get the first seller ID from sellers table
        SELECT seller_id INTO first_seller_id 
        FROM web_store.sellers 
        ORDER BY seller_id ASC 
        LIMIT 1;
        
        -- If sellers exist, assign the first seller to all products with NULL seller_id
        IF first_seller_id IS NOT NULL THEN
            -- Update all products that have NULL seller_id
            UPDATE web_store.products 
            SET seller_id = first_seller_id,
                updated_at = CURRENT_TIMESTAMP,
                updated_by = COALESCE(updated_by, 'system')
            WHERE seller_id IS NULL;
            
            GET DIAGNOSTICS products_updated = ROW_COUNT;
            
            RAISE NOTICE 'Assigned seller_id % to % existing products with NULL seller_id', first_seller_id, products_updated;
        ELSE
            RAISE EXCEPTION 'No sellers found in sellers table. Please create at least one seller before running this migration.';
        END IF;
    ELSE
        RAISE NOTICE 'No products with NULL seller_id found. All products already have seller_id assigned.';
    END IF;
END $$;

-- Verify that all products now have seller_id assigned
DO $$
DECLARE
    null_count INT;
BEGIN
    SELECT COUNT(*) INTO null_count 
    FROM web_store.products 
    WHERE seller_id IS NULL;
    
    IF null_count > 0 THEN
        RAISE WARNING 'Warning: % products still have NULL seller_id after update', null_count;
    ELSE
        RAISE NOTICE 'Success: All products now have seller_id assigned';
    END IF;
END $$;

