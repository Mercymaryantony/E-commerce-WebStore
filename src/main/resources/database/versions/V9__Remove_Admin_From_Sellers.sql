-- =====================================================
-- Flyway Migration V9: Remove ADMIN Role from Sellers Table
-- =====================================================
-- Purpose: Remove ADMIN role from sellers table constraint
--          Sellers table should only contain SELLER role
--          Admin users are now managed in the users table
-- =====================================================

-- Step 1: Update any existing ADMIN sellers to SELLER (if any exist)
UPDATE web_store.sellers 
SET role = 'SELLER' 
WHERE role = 'ADMIN';

-- Step 2: Drop the existing constraint that allows both ADMIN and SELLER
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_schema = 'web_store' 
        AND table_name = 'sellers' 
        AND constraint_name = 'chk_seller_role'
    ) THEN
        ALTER TABLE web_store.sellers 
        DROP CONSTRAINT chk_seller_role;
    END IF;
END $$;

-- Step 3: Add new constraint to only allow SELLER role
ALTER TABLE web_store.sellers 
ADD CONSTRAINT chk_seller_role 
CHECK (role = 'SELLER');

-- =====================================================
-- VERIFICATION QUERIES (Run these to verify)
-- =====================================================
--
-- After migration runs, you can verify with these queries:
--
-- 1. Check constraint exists:
-- SELECT constraint_name, check_clause
-- FROM information_schema.check_constraints
-- WHERE constraint_schema = 'web_store' 
-- AND constraint_name = 'chk_seller_role';
--
-- 2. Verify all sellers have SELLER role:
-- SELECT role, COUNT(*) 
-- FROM web_store.sellers 
-- GROUP BY role;
--
-- 3. Try to insert with ADMIN role (should fail):
-- INSERT INTO web_store.sellers (name, email, status, role, joining_date)
-- VALUES ('Test', 'test@test.com', 'ACTIVE', 'ADMIN', CURRENT_DATE);
-- (This should fail with constraint violation)
--
-- =====================================================

