-- ============================================================================
-- Transportation Management System - Initialize Denormalized Tables
-- ============================================================================
-- This script initializes denormalized tables after the schema is created.
-- It should run after all tables, functions, and triggers are created.
-- ============================================================================

-- Initialize route availability for all existing reservations
-- Creates availability entries for each unique (route_id, departure_time) combination
-- Uses calculate_booked_seats() to correctly handle time-based overlap
DO $$
DECLARE
    reservation_record RECORD;
BEGIN
    FOR reservation_record IN 
        SELECT DISTINCT route_id, departure_time, arrival_time
        FROM reservations
        WHERE status IN ('PENDING', 'CONFIRMED')
        ORDER BY route_id, departure_time
    LOOP
        BEGIN
            CALL update_route_availability(
                reservation_record.route_id,
                reservation_record.departure_time,
                reservation_record.arrival_time
            );
        EXCEPTION
            WHEN OTHERS THEN
                -- Route availability might already exist, continue
                NULL;
        END;
    END LOOP;
END $$;

-- Initialize route statistics for all existing routes
DO $$
DECLARE
    route_record RECORD;
BEGIN
    FOR route_record IN SELECT id FROM routes LOOP
        BEGIN
            CALL update_route_statistics(route_record.id);
        EXCEPTION
            WHEN OTHERS THEN
                -- Route statistics might already exist, continue
                NULL;
        END;
    END LOOP;
END $$;

-- Note: If you want to load sample data, you can run 04_sample_data.sql separately
-- after the database is initialized, or uncomment the sample data insertion below.

