-- ============================================================================
-- Transportation Management System - Master Database Initialization Script
-- ============================================================================
-- This script initializes the entire database by running all SQL scripts in order.
-- Execute this script to set up the complete database schema.
-- 
-- Usage:
--   psql -U username -d database_name -f 00_init_database.sql
-- ============================================================================

\echo '================================================================================'
\echo 'Transportation Management System - Database Initialization'
\echo '================================================================================'
\echo ''

-- Set error handling
\set ON_ERROR_STOP on

-- ============================================================================
-- STEP 1: Create Core Schema
-- ============================================================================
\echo 'Step 1: Creating core database schema...'
\i 01_schema_core.sql
\echo '✓ Core schema created successfully'
\echo ''

-- ============================================================================
-- STEP 2: Create Functions and Procedures
-- ============================================================================
\echo 'Step 2: Creating functions and procedures...'
\i 02_functions_procedures.sql
\echo '✓ Functions and procedures created successfully'
\echo ''

-- ============================================================================
-- STEP 3: Create Triggers
-- ============================================================================
\echo 'Step 3: Creating triggers...'
\i 03_triggers.sql
\echo '✓ Triggers created successfully'
\echo ''

-- ============================================================================
-- STEP 4: Insert Sample Data (Optional)
-- ============================================================================
\echo 'Step 4: Inserting sample data...'
\echo '  (Skipping sample data insertion - uncomment the line below to include it)'
-- \i 04_sample_data.sql
\echo '✓ Sample data insertion completed (or skipped)'
\echo ''

-- ============================================================================
-- STEP 5: Initialize Denormalized Tables
-- ============================================================================
\echo 'Step 5: Initializing denormalized tables...'
CALL initialize_all_route_availability();
CALL initialize_all_route_statistics();
\echo '✓ Denormalized tables initialized successfully'
\echo ''

-- ============================================================================
-- VERIFICATION
-- ============================================================================
\echo '================================================================================'
\echo 'Database Initialization Complete!'
\echo '================================================================================'
\echo ''
\echo 'Verification:'
\echo '  - Tables created:'
SELECT COUNT(*) AS table_count FROM information_schema.tables 
WHERE table_schema = 'public' AND table_type = 'BASE TABLE';
\echo ''
\echo '  - Functions created:'
SELECT COUNT(*) AS function_count FROM information_schema.routines 
WHERE routine_schema = 'public' AND routine_type = 'FUNCTION';
\echo ''
\echo '  - Procedures created:'
SELECT COUNT(*) AS procedure_count FROM information_schema.routines 
WHERE routine_schema = 'public' AND routine_type = 'PROCEDURE';
\echo ''
\echo '  - Triggers created:'
SELECT COUNT(*) AS trigger_count FROM information_schema.triggers 
WHERE trigger_schema = 'public';
\echo ''
\echo '================================================================================'

