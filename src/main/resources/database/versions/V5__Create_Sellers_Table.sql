-- =====================================================
-- Flyway Migration V5: Create Sellers Table
-- =====================================================
-- Purpose: Add seller management functionality
-- =====================================================
-- Create Sequence for Seller IDs
-- =====================================================

CREATE SEQUENCE IF NOT EXISTS web_store.seq_seller_id 
    START WITH 1 
    INCREMENT BY 1;

-- =====================================================
-- Create Sellers Table
-- =====================================================

CREATE TABLE IF NOT EXISTS web_store.sellers (
    -- Primary Key Column
    seller_id INT NOT NULL DEFAULT nextval('web_store.seq_seller_id') PRIMARY KEY,
    
    -- Business Columns
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    joining_date DATE NOT NULL,
    
    -- Audit Columns (who created/updated and when)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    
    -- Constraints
    CONSTRAINT uk_seller_email UNIQUE (email)
);

-- =====================================================
--  Create Indexes for Performance
-- We create indexes on:
-- 1. email: Used for searching/checking uniqueness
-- 2. status: Used for filtering ACTIVE/INACTIVE sellers
-- 3. joining_date: Used for date range queries
-- =====================================================

CREATE INDEX idx_seller_email ON web_store.sellers(email);
CREATE INDEX idx_seller_status ON web_store.sellers(status);
CREATE INDEX idx_seller_joining_date ON web_store.sellers(joining_date);

-- =====================================================
--  Add Comment to Table (Documentation)
-- =====================================================
COMMENT ON TABLE web_store.sellers IS 
'Sellers/vendors in the system. Used for admin dashboard seller management. Standalone table not linked to products.';

-- =====================================================
-- Insert Test Data (For Development Only)
-- =====================================================

INSERT INTO web_store.sellers (name, email, status, joining_date, created_by, updated_by)
VALUES
    ('John Doe', 'john.doe@example.com', 'ACTIVE', '2024-01-15', 'system', 'system'),
    ('Jane Smith', 'jane.smith@example.com', 'ACTIVE', '2024-02-20', 'system', 'system'),
    ('Bob Wilson', 'bob.wilson@example.com', 'INACTIVE', '2024-03-10', 'system', 'system'),
    ('Alice Brown', 'alice.brown@example.com', 'ACTIVE', '2024-04-05', 'system', 'system'),
    ('Charlie Davis', 'charlie.davis@example.com', 'ACTIVE', '2024-05-12', 'system', 'system');

-- =====================================================
-- VERIFICATION QUERIES (Run these to verify)
-- =====================================================
--
-- After migration runs, you can verify with these queries:
--
-- 1. Check if table exists:
-- SELECT * FROM information_schema.tables WHERE table_name = 'sellers';
--
-- 2. Check table structure:
-- SELECT column_name, data_type, character_maximum_length, is_nullable
-- FROM information_schema.columns
-- WHERE table_name = 'sellers'
-- ORDER BY ordinal_position;
--
-- 3. View test data:
-- SELECT * FROM web_store.sellers ORDER BY seller_id;
--
-- 4. Count sellers by status:
-- SELECT status, COUNT(*) FROM web_store.sellers GROUP BY status;
--
-- =====================================================
-- ROLLBACK SCRIPT (If you need to undo this migration)
-- =====================================================
--
-- WARNING: This will delete all seller data!
-- Only use in development/testing!
--
-- DROP INDEX IF EXISTS web_store.idx_seller_email;
-- DROP INDEX IF EXISTS web_store.idx_seller_status;
-- DROP INDEX IF EXISTS web_store.idx_seller_joining_date;
-- DROP TABLE IF EXISTS web_store.sellers;
-- DROP SEQUENCE IF EXISTS web_store.seq_seller_id;
--
-- =====================================================

