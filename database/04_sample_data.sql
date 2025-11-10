-- ============================================================================
-- Transportation Management System - Sample Data
-- ============================================================================
-- This script inserts sample data for testing and demonstration purposes.
-- ============================================================================

-- ============================================================================
-- SAMPLE STATIONS
-- ============================================================================

INSERT INTO stations (name, description, status, latitude, longitude, address, created_by) VALUES
('Central Station', 'Main transportation hub in the city center', 'ACTIVE', 40.7128, -74.0060, '123 Main Street, City Center', 'ADMIN'),
('North Terminal', 'Northern terminal serving suburban areas', 'ACTIVE', 40.7589, -73.9851, '456 North Avenue, Suburbs', 'ADMIN'),
('South Station', 'Southern terminal serving industrial areas', 'ACTIVE', 40.6892, -74.0445, '789 South Boulevard, Industrial District', 'ADMIN'),
('East Depot', 'Eastern depot serving residential areas', 'ACTIVE', 40.7484, -73.9857, '321 East Road, Residential Area', 'ADMIN'),
('West Hub', 'Western hub serving commercial districts', 'ACTIVE', 40.7614, -73.9776, '654 West Street, Commercial District', 'ADMIN'),
('Airport Terminal', 'Terminal serving the international airport', 'ACTIVE', 40.6413, -73.7781, 'Airport Road, Airport District', 'ADMIN');

-- ============================================================================
-- SAMPLE ROUTES
-- ============================================================================

-- Get station IDs (using subqueries)
INSERT INTO routes (origin_station_id, destination_station_id, vehicle_capacity, distance_km, estimated_duration_minutes, status, created_by) VALUES
((SELECT id FROM stations WHERE name = 'Central Station'), (SELECT id FROM stations WHERE name = 'North Terminal'), 50, 25.5, 45, 'ACTIVE', 'ADMIN'),
((SELECT id FROM stations WHERE name = 'Central Station'), (SELECT id FROM stations WHERE name = 'South Station'), 50, 30.2, 50, 'ACTIVE', 'ADMIN'),
((SELECT id FROM stations WHERE name = 'Central Station'), (SELECT id FROM stations WHERE name = 'East Depot'), 40, 20.0, 35, 'ACTIVE', 'ADMIN'),
((SELECT id FROM stations WHERE name = 'Central Station'), (SELECT id FROM stations WHERE name = 'West Hub'), 50, 18.7, 30, 'ACTIVE', 'ADMIN'),
((SELECT id FROM stations WHERE name = 'Central Station'), (SELECT id FROM stations WHERE name = 'Airport Terminal'), 60, 45.0, 60, 'ACTIVE', 'ADMIN'),
((SELECT id FROM stations WHERE name = 'North Terminal'), (SELECT id FROM stations WHERE name = 'South Station'), 50, 55.0, 90, 'ACTIVE', 'ADMIN'),
((SELECT id FROM stations WHERE name = 'North Terminal'), (SELECT id FROM stations WHERE name = 'Airport Terminal'), 60, 65.0, 75, 'ACTIVE', 'ADMIN'),
((SELECT id FROM stations WHERE name = 'East Depot'), (SELECT id FROM stations WHERE name = 'West Hub'), 40, 38.5, 55, 'ACTIVE', 'ADMIN'),
((SELECT id FROM stations WHERE name = 'South Station'), (SELECT id FROM stations WHERE name = 'Airport Terminal'), 60, 50.0, 70, 'ACTIVE', 'ADMIN');

-- ============================================================================
-- SAMPLE ROUTE TIMETABLES
-- ============================================================================

WITH route_ct_to_nt AS (
    SELECT r.id
    FROM routes r
    JOIN stations s1 ON r.origin_station_id = s1.id
    JOIN stations s2 ON r.destination_station_id = s2.id
    WHERE s1.name = 'Central Station' AND s2.name = 'North Terminal'
    LIMIT 1
), weekday_timetable AS (
    INSERT INTO route_timetables (route_id, name, description, effective_from, status, created_by)
    SELECT id, 'Weekday Morning Schedule', 'Morning commuter departures Monday to Friday', '2025-01-01', 'ACTIVE', 'ADMIN'
    FROM route_ct_to_nt
    RETURNING id
)
INSERT INTO route_timetable_entries (timetable_id, service_day, departure_time, arrival_time, notes)
SELECT weekday_timetable.id, v.service_day, v.departure_time, v.arrival_time, v.notes
FROM weekday_timetable
CROSS JOIN (
    VALUES
        ('MONDAY',  TIME '07:30', TIME '08:15', 'Morning commuter express'),
        ('TUESDAY', TIME '07:30', TIME '08:15', 'Morning commuter express'),
        ('WEDNESDAY', TIME '07:30', TIME '08:15', 'Morning commuter express'),
        ('THURSDAY', TIME '07:30', TIME '08:15', 'Morning commuter express'),
        ('FRIDAY', TIME '07:30', TIME '08:15', 'Morning commuter express'),
        ('MONDAY',  TIME '18:00', TIME '18:45', 'Evening return service'),
        ('TUESDAY', TIME '18:00', TIME '18:45', 'Evening return service'),
        ('WEDNESDAY', TIME '18:00', TIME '18:45', 'Evening return service'),
        ('THURSDAY', TIME '18:00', TIME '18:45', 'Evening return service'),
        ('FRIDAY', TIME '18:00', TIME '18:45', 'Evening return service')
) AS v(service_day, departure_time, arrival_time, notes);

WITH route_ct_to_airport AS (
    SELECT r.id
    FROM routes r
    JOIN stations s1 ON r.origin_station_id = s1.id
    JOIN stations s2 ON r.destination_station_id = s2.id
    WHERE s1.name = 'Central Station' AND s2.name = 'Airport Terminal'
    LIMIT 1
), weekend_timetable AS (
    INSERT INTO route_timetables (route_id, name, description, effective_from, status, created_by)
    SELECT id, 'Weekend Airport Shuttle', 'Frequent airport runs on weekends', '2025-01-01', 'ACTIVE', 'ADMIN'
    FROM route_ct_to_airport
    RETURNING id
)
INSERT INTO route_timetable_entries (timetable_id, service_day, departure_time, arrival_time, notes)
SELECT weekend_timetable.id, v.service_day, v.departure_time, v.arrival_time, v.notes
FROM weekend_timetable
CROSS JOIN (
    VALUES
        ('SATURDAY', TIME '06:00', TIME '06:50', 'Early shuttle'),
        ('SATURDAY', TIME '10:00', TIME '10:50', 'Mid-morning shuttle'),
        ('SATURDAY', TIME '14:00', TIME '14:50', 'Afternoon shuttle'),
        ('SATURDAY', TIME '18:00', TIME '18:50', 'Evening shuttle'),
        ('SUNDAY', TIME '06:00', TIME '06:50', 'Early shuttle'),
        ('SUNDAY', TIME '10:00', TIME '10:50', 'Mid-morning shuttle'),
        ('SUNDAY', TIME '14:00', TIME '14:50', 'Afternoon shuttle'),
        ('SUNDAY', TIME '18:00', TIME '18:50', 'Evening shuttle')
) AS v(service_day, departure_time, arrival_time, notes);

-- ============================================================================
-- SAMPLE FARE POLICIES
-- ============================================================================

-- Insert fare policies for different routes, categories, and classes
-- Base prices vary by route distance, vehicle class, and passenger category
-- Discounts are already factored into the base_price for each category
INSERT INTO fare_policies (route_id, passenger_category, vehicle_class, base_price, effective_from, status, created_by)
SELECT 
    r.id,
    pc.category,
    vc.class,
    -- Calculate base price: start with ADULT price, then apply category discount
    (
        CASE 
            -- ADULT prices (no discount) vary by route distance and vehicle class
            WHEN COALESCE(r.distance_km, 0) <= 25 THEN 
                CASE vc.class
                    WHEN 'STANDARD' THEN 15.00
                    WHEN 'COACH' THEN 25.00
                    WHEN 'MINI_BUS' THEN 12.00
                    WHEN 'DOUBLE_DECKER' THEN 30.00
                END
            WHEN COALESCE(r.distance_km, 0) <= 40 THEN
                CASE vc.class
                    WHEN 'STANDARD' THEN 25.00
                    WHEN 'COACH' THEN 40.00
                    WHEN 'MINI_BUS' THEN 20.00
                    WHEN 'DOUBLE_DECKER' THEN 50.00
                END
            ELSE
                CASE vc.class
                    WHEN 'STANDARD' THEN 35.00
                    WHEN 'COACH' THEN 55.00
                    WHEN 'MINI_BUS' THEN 30.00
                    WHEN 'DOUBLE_DECKER' THEN 70.00
                END
        END * (1 - COALESCE(pc.discount_percentage, 0) / 100.0)
    ) AS base_price,
    '2024-01-01 00:00:00'::TIMESTAMP,
    'ACTIVE',
    'ADMIN'
FROM routes r
CROSS JOIN passenger_categories pc
CROSS JOIN vehicle_classes vc
WHERE r.status = 'ACTIVE';

-- ============================================================================
-- SAMPLE RESERVATIONS
-- ============================================================================

-- Insert some sample reservations
-- Note: These will trigger the denormalization and availability updates

-- Get a sample route ID
DO $$
DECLARE
    v_route_id BIGINT;
    v_departure_time TIMESTAMP;
    v_arrival_time TIMESTAMP;
BEGIN
    -- Get first route
    SELECT id INTO v_route_id FROM routes LIMIT 1;
    
    -- Create reservations for the next few days
    FOR i IN 1..5 LOOP
        v_departure_time := CURRENT_TIMESTAMP + (i || ' days')::INTERVAL + '09:00:00'::TIME;
        v_arrival_time := v_departure_time + '45 minutes'::INTERVAL;
        
        INSERT INTO reservations (
            route_id,
            passenger_name,
            passenger_email,
            passenger_phone,
            seat_count,
            status,
            passenger_category,
            vehicle_class,
            departure_time,
            arrival_time,
            created_by
        ) VALUES (
            v_route_id,
            'Passenger ' || i,
            'passenger' || i || '@example.com',
            '+1234567890' || i,
            1 + (i % 3), -- Vary seat count
            CASE WHEN i <= 3 THEN 'CONFIRMED' ELSE 'PENDING' END,
            CASE (i % 4)
                WHEN 0 THEN 'ADULT'
                WHEN 1 THEN 'CHILD'
                WHEN 2 THEN 'SENIOR'
                ELSE 'STUDENT'
            END,
            CASE (i % 4)
                WHEN 0 THEN 'STANDARD'
                WHEN 1 THEN 'COACH'
                WHEN 2 THEN 'MINI_BUS'
                ELSE 'DOUBLE_DECKER'
            END,
            v_departure_time,
            v_arrival_time,
            'ADMIN'
        );
    END LOOP;
END $$;

-- ============================================================================
-- VERIFICATION QUERIES (commented out - uncomment to verify)
-- ============================================================================

/*
-- Verify stations
SELECT * FROM stations;

-- Verify routes
SELECT r.id, origin.name AS origin, destination.name AS destination, r.vehicle_capacity, r.status
FROM routes r
JOIN stations origin ON r.origin_station_id = origin.id
JOIN stations destination ON r.destination_station_id = destination.id;

-- Verify fare policies
SELECT fp.id, 
       origin.name || ' -> ' || destination.name AS route,
       fp.passenger_category,
       fp.vehicle_class,
       fp.base_price,
       fp.status
FROM fare_policies fp
JOIN routes r ON fp.route_id = r.id
JOIN stations origin ON r.origin_station_id = origin.id
JOIN stations destination ON r.destination_station_id = destination.id
LIMIT 20;

-- Verify reservations
SELECT res.id,
       res.passenger_name,
       res.origin_station_name || ' -> ' || res.destination_station_name AS route,
       res.seat_count,
       res.status,
       res.base_fare,
       res.vat_amount,
       res.total_fare,
       res.departure_time
FROM reservations res;

-- Verify route availability
SELECT ra.route_id,
       origin.name || ' -> ' || destination.name AS route,
       ra.total_capacity,
       ra.booked_seats,
       ra.available_seats
FROM route_availability ra
JOIN routes r ON ra.route_id = r.id
JOIN stations origin ON r.origin_station_id = origin.id
JOIN stations destination ON r.destination_station_id = destination.id;

-- Verify route statistics
SELECT rs.route_id,
       origin.name || ' -> ' || destination.name AS route,
       rs.total_reservations,
       rs.confirmed_reservations,
       rs.total_revenue,
       rs.average_occupancy_rate
FROM route_statistics rs
JOIN routes r ON rs.route_id = r.id
JOIN stations origin ON r.origin_station_id = origin.id
JOIN stations destination ON r.destination_station_id = destination.id;
*/

