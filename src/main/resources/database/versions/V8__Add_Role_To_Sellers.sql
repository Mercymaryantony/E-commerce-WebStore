-- =====================================================
-- Flyway Migration V8: Add Role Field to Sellers Table
-- =====================================================
-- This migration adds a role field to distinguish between ADMIN and SELLER

-- Step 1: Add role column to sellers table (nullable first to handle existing data)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'web_store' 
        AND table_name = 'sellers' 
        AND column_name = 'role'
    ) THEN
        ALTER TABLE web_store.sellers 
        ADD COLUMN role VARCHAR(20);
    END IF;
END $$;

-- Step 2: Update existing sellers to have SELLER role (if any exist)
UPDATE web_store.sellers 
SET role = 'SELLER' 
WHERE role IS NULL OR role = '';

-- Step 3: Make the column NOT NULL with default value
ALTER TABLE web_store.sellers 
ALTER COLUMN role SET NOT NULL,
ALTER COLUMN role SET DEFAULT 'SELLER';

-- Step 4: Add constraint to ensure role is either ADMIN or SELLER (if it doesn't exist)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_schema = 'web_store' 
        AND table_name = 'sellers' 
        AND constraint_name = 'chk_seller_role'
    ) THEN
        ALTER TABLE web_store.sellers 
        ADD CONSTRAINT chk_seller_role 
        CHECK (role IN ('ADMIN', 'SELLER'));
    END IF;
END $$;