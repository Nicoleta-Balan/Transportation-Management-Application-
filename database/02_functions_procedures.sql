-- ============================================================================
-- Transportation Management System - Functions and Procedures
-- ============================================================================
-- This script creates functions and stored procedures for data processing,
-- calculations, and business logic.
-- ============================================================================

-- ============================================================================
-- HELPER FUNCTIONS
-- ============================================================================

-- Function to get current VAT rate
CREATE OR REPLACE FUNCTION get_current_vat_rate()
RETURNS DECIMAL(5, 2) AS $$
DECLARE
    current_rate DECIMAL(5, 2);
BEGIN
    SELECT rate_percentage INTO current_rate
    FROM vat_rates
    WHERE effective_from <= CURRENT_TIMESTAMP
      AND (effective_to IS NULL OR effective_to > CURRENT_TIMESTAMP)
    ORDER BY effective_from DESC
    LIMIT 1;
    
    -- If no VAT rate found, return 0
    IF current_rate IS NULL THEN
        RETURN 0.00;
    END IF;
    
    RETURN current_rate;
END;
$$ LANGUAGE plpgsql;

-- Function to get VAT rate for a specific date
CREATE OR REPLACE FUNCTION get_vat_rate_for_date(check_date TIMESTAMP)
RETURNS DECIMAL(5, 2) AS $$
DECLARE
    rate DECIMAL(5, 2);
BEGIN
    SELECT rate_percentage INTO rate
    FROM vat_rates
    WHERE effective_from <= check_date
      AND (effective_to IS NULL OR effective_to > check_date)
    ORDER BY effective_from DESC
    LIMIT 1;
    
    IF rate IS NULL THEN
        RETURN 0.00;
    END IF;
    
    RETURN rate;
END;
$$ LANGUAGE plpgsql;

-- Function to get active fare policy for a route, category, and class
CREATE OR REPLACE FUNCTION get_active_fare_policy(
    p_route_id BIGINT,
    p_passenger_category VARCHAR(20),
    p_vehicle_class VARCHAR(20),
    p_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
RETURNS DECIMAL(10, 2) AS $$
DECLARE
    fare_price DECIMAL(10, 2);
BEGIN
    SELECT base_price INTO fare_price
    FROM fare_policies
    WHERE route_id = p_route_id
      AND passenger_category = p_passenger_category
      AND vehicle_class = p_vehicle_class
      AND status = 'ACTIVE'
      AND effective_from <= p_date
      AND (effective_to IS NULL OR effective_to > p_date)
    ORDER BY effective_from DESC
    LIMIT 1;
    
    IF fare_price IS NULL THEN
        RAISE EXCEPTION 'No active fare policy found for route %, category %, class %', 
            p_route_id, p_passenger_category, p_vehicle_class;
    END IF;
    
    RETURN fare_price;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- FARE CALCULATION FUNCTIONS
-- ============================================================================

-- Function to calculate fare with VAT
CREATE OR REPLACE FUNCTION calculate_fare_with_vat(
    p_base_price DECIMAL(10, 2),
    p_seat_count INTEGER,
    p_vat_rate DECIMAL(5, 2) DEFAULT NULL
)
RETURNS TABLE (
    base_fare DECIMAL(10, 2),
    vat_amount DECIMAL(10, 2),
    total_fare DECIMAL(10, 2)
) AS $$
DECLARE
    v_vat_rate DECIMAL(5, 2);
    v_base_total DECIMAL(10, 2);
    v_vat_amount DECIMAL(10, 2);
    v_total_fare DECIMAL(10, 2);
BEGIN
    -- Get VAT rate if not provided
    IF p_vat_rate IS NULL THEN
        v_vat_rate := get_current_vat_rate();
    ELSE
        v_vat_rate := p_vat_rate;
    END IF;
    
    -- Calculate base fare total
    v_base_total := p_base_price * p_seat_count;
    
    -- Calculate VAT amount
    v_vat_amount := ROUND(v_base_total * (v_vat_rate / 100.0), 2);
    
    -- Calculate total fare
    v_total_fare := v_base_total + v_vat_amount;
    
    RETURN QUERY SELECT v_base_total, v_vat_amount, v_total_fare;
END;
$$ LANGUAGE plpgsql;

-- Function to calculate fare for a reservation
CREATE OR REPLACE FUNCTION calculate_reservation_fare(
    p_route_id BIGINT,
    p_passenger_category VARCHAR(20),
    p_vehicle_class VARCHAR(20),
    p_seat_count INTEGER,
    p_reservation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
RETURNS TABLE (
    base_fare DECIMAL(10, 2),
    vat_amount DECIMAL(10, 2),
    total_fare DECIMAL(10, 2),
    vat_rate DECIMAL(5, 2)
) AS $$
DECLARE
    v_base_price DECIMAL(10, 2);
    v_vat_rate DECIMAL(5, 2);
    v_calculated_fare RECORD;
BEGIN
    -- Get base price
    v_base_price := get_active_fare_policy(p_route_id, p_passenger_category, p_vehicle_class, p_reservation_date);
    
    -- Get VAT rate for the reservation date
    v_vat_rate := get_vat_rate_for_date(p_reservation_date);
    
    -- Calculate fare with VAT
    SELECT * INTO v_calculated_fare
    FROM calculate_fare_with_vat(v_base_price, p_seat_count, v_vat_rate);
    
    RETURN QUERY SELECT 
        v_calculated_fare.base_fare,
        v_calculated_fare.vat_amount,
        v_calculated_fare.total_fare,
        v_vat_rate;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- AVAILABILITY CHECK FUNCTIONS
-- ============================================================================

-- Function to check seat availability for a route at a specific departure time
CREATE OR REPLACE FUNCTION check_seat_availability(
    p_route_id BIGINT,
    p_departure_time TIMESTAMP,
    p_required_seats INTEGER
)
RETURNS BOOLEAN AS $$
DECLARE
    v_available_seats INTEGER;
    v_total_capacity INTEGER;
    v_booked_seats INTEGER;
BEGIN
    SELECT available_seats INTO v_available_seats
    FROM route_availability
    WHERE route_id = p_route_id
      AND departure_time = p_departure_time;
    
    -- If route_availability doesn't exist for this departure time, calculate it
    IF v_available_seats IS NULL THEN
        -- Get route capacity
        SELECT vehicle_capacity INTO v_total_capacity
        FROM routes
        WHERE id = p_route_id;
        
        IF v_total_capacity IS NULL THEN
            RETURN FALSE;
        END IF;
        
        -- Calculate booked seats for this time period
        -- Note: Using default 1 hour interval if arrival time not known
        v_booked_seats := calculate_booked_seats(
            p_route_id, 
            p_departure_time, 
            p_departure_time + INTERVAL '1 hour'
        );
        
        v_available_seats := GREATEST(0, v_total_capacity - v_booked_seats);
    END IF;
    
    RETURN v_available_seats >= p_required_seats;
END;
$$ LANGUAGE plpgsql;

-- Function to get available seats for a route at a specific departure time
CREATE OR REPLACE FUNCTION get_available_seats(
    p_route_id BIGINT,
    p_departure_time TIMESTAMP
)
RETURNS INTEGER AS $$
DECLARE
    v_available_seats INTEGER;
    v_total_capacity INTEGER;
    v_booked_seats INTEGER;
BEGIN
    SELECT available_seats INTO v_available_seats
    FROM route_availability
    WHERE route_id = p_route_id
      AND departure_time = p_departure_time;
    
    IF v_available_seats IS NULL THEN
        -- Calculate on-the-fly if not cached
        SELECT vehicle_capacity INTO v_total_capacity
        FROM routes
        WHERE id = p_route_id;
        
        IF v_total_capacity IS NULL THEN
            RETURN 0;
        END IF;
        
        v_booked_seats := calculate_booked_seats(
            p_route_id, 
            p_departure_time, 
            p_departure_time + INTERVAL '1 hour'
        );
        
        RETURN GREATEST(0, v_total_capacity - v_booked_seats);
    END IF;
    
    RETURN v_available_seats;
END;
$$ LANGUAGE plpgsql;

-- Function to calculate booked seats for a route and time period
-- Returns seats booked for reservations that overlap with the given time period
CREATE OR REPLACE FUNCTION calculate_booked_seats(
    p_route_id BIGINT,
    p_departure_time TIMESTAMP,
    p_arrival_time TIMESTAMP
)
RETURNS INTEGER AS $$
DECLARE
    v_booked_seats INTEGER;
BEGIN
    -- Calculate seats for overlapping reservations
    -- Two reservations overlap if: p.departure < res.arrival AND p.arrival > res.departure
    SELECT COALESCE(SUM(seat_count), 0) INTO v_booked_seats
    FROM reservations
    WHERE route_id = p_route_id
      AND status IN ('PENDING', 'CONFIRMED')
      AND departure_time < p_arrival_time
      AND arrival_time > p_departure_time;
    
    RETURN v_booked_seats;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- REVENUE CALCULATION FUNCTIONS
-- ============================================================================

-- Function to calculate total revenue for confirmed reservations
CREATE OR REPLACE FUNCTION calculate_total_revenue()
RETURNS DECIMAL(15, 2) AS $$
DECLARE
    v_total_revenue DECIMAL(15, 2);
BEGIN
    SELECT COALESCE(SUM(total_fare), 0.00) INTO v_total_revenue
    FROM reservations
    WHERE status = 'CONFIRMED';
    
    RETURN v_total_revenue;
END;
$$ LANGUAGE plpgsql;

-- Function to calculate revenue for a specific route
CREATE OR REPLACE FUNCTION calculate_route_revenue(p_route_id BIGINT)
RETURNS DECIMAL(15, 2) AS $$
DECLARE
    v_revenue DECIMAL(15, 2);
BEGIN
    SELECT COALESCE(SUM(total_fare), 0.00) INTO v_revenue
    FROM reservations
    WHERE route_id = p_route_id
      AND status = 'CONFIRMED';
    
    RETURN v_revenue;
END;
$$ LANGUAGE plpgsql;

-- Function to calculate revenue for a date range
CREATE OR REPLACE FUNCTION calculate_revenue_for_period(
    p_start_date TIMESTAMP,
    p_end_date TIMESTAMP
)
RETURNS TABLE (
    total_revenue DECIMAL(15, 2),
    total_vat DECIMAL(15, 2),
    net_revenue DECIMAL(15, 2),
    reservation_count BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COALESCE(SUM(total_fare), 0.00) AS total_revenue,
        COALESCE(SUM(vat_amount), 0.00) AS total_vat,
        COALESCE(SUM(base_fare), 0.00) AS net_revenue,
        COUNT(*) AS reservation_count
    FROM reservations
    WHERE status = 'CONFIRMED'
      AND created_at BETWEEN p_start_date AND p_end_date;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- STATISTICS CALCULATION FUNCTIONS
-- ============================================================================

-- Function to calculate route statistics
CREATE OR REPLACE FUNCTION calculate_route_statistics(p_route_id BIGINT)
RETURNS TABLE (
    total_reservations BIGINT,
    confirmed_reservations BIGINT,
    cancelled_reservations BIGINT,
    total_revenue DECIMAL(15, 2),
    average_occupancy_rate DECIMAL(5, 2)
) AS $$
DECLARE
    v_total_capacity INTEGER;
    v_total_reservations BIGINT;
    v_confirmed_reservations BIGINT;
    v_cancelled_reservations BIGINT;
    v_total_revenue DECIMAL(15, 2);
    v_avg_occupancy DECIMAL(5, 2);
BEGIN
    -- Get route capacity
    SELECT vehicle_capacity INTO v_total_capacity
    FROM routes
    WHERE id = p_route_id;
    
    IF v_total_capacity IS NULL THEN
        RAISE EXCEPTION 'Route % not found', p_route_id;
    END IF;
    
    -- Calculate statistics
    SELECT 
        COUNT(*),
        COUNT(*) FILTER (WHERE status = 'CONFIRMED'),
        COUNT(*) FILTER (WHERE status = 'CANCELLED'),
        COALESCE(SUM(total_fare) FILTER (WHERE status = 'CONFIRMED'), 0.00)
    INTO 
        v_total_reservations,
        v_confirmed_reservations,
        v_cancelled_reservations,
        v_total_revenue
    FROM reservations
    WHERE route_id = p_route_id;
    
    -- Calculate average occupancy (simplified: based on confirmed reservations)
    IF v_total_reservations > 0 THEN
        SELECT COALESCE(SUM(seat_count), 0) INTO v_avg_occupancy
        FROM reservations
        WHERE route_id = p_route_id AND status = 'CONFIRMED';
        
        v_avg_occupancy := ROUND((v_avg_occupancy::DECIMAL / v_total_capacity::DECIMAL) * 100, 2);
    ELSE
        v_avg_occupancy := 0.00;
    END IF;
    
    RETURN QUERY SELECT 
        v_total_reservations,
        v_confirmed_reservations,
        v_cancelled_reservations,
        v_total_revenue,
        v_avg_occupancy;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- PROCEDURES
-- ============================================================================

-- Procedure to update route availability for a specific departure time
-- Uses calculate_booked_seats() to correctly handle time-based overlap
CREATE OR REPLACE PROCEDURE update_route_availability(
    p_route_id BIGINT,
    p_departure_time TIMESTAMP,
    p_arrival_time TIMESTAMP
)
LANGUAGE plpgsql AS $$
DECLARE
    v_total_capacity INTEGER;
    v_booked_seats INTEGER;
    v_available_seats INTEGER;
BEGIN
    -- Get route capacity
    SELECT vehicle_capacity INTO v_total_capacity
    FROM routes
    WHERE id = p_route_id;
    
    IF v_total_capacity IS NULL THEN
        RAISE EXCEPTION 'Route % not found', p_route_id;
    END IF;
    
    -- Use calculate_booked_seats() to get seats booked for overlapping time periods
    -- This correctly handles multiple trips on the same route at different times
    v_booked_seats := calculate_booked_seats(p_route_id, p_departure_time, p_arrival_time);
    
    -- Calculate available seats
    v_available_seats := GREATEST(0, v_total_capacity - v_booked_seats);
    
    -- Insert or update route availability for this specific departure time
    INSERT INTO route_availability (
        route_id, 
        departure_time, 
        arrival_time,
        total_capacity, 
        booked_seats, 
        available_seats, 
        last_updated
    )
    VALUES (
        p_route_id, 
        p_departure_time, 
        p_arrival_time,
        v_total_capacity, 
        v_booked_seats, 
        v_available_seats, 
        CURRENT_TIMESTAMP
    )
    ON CONFLICT (route_id, departure_time) 
    DO UPDATE SET
        arrival_time = EXCLUDED.arrival_time,
        total_capacity = EXCLUDED.total_capacity,
        booked_seats = EXCLUDED.booked_seats,
        available_seats = EXCLUDED.available_seats,
        last_updated = CURRENT_TIMESTAMP;
END;
$$;

-- Procedure to update route statistics
CREATE OR REPLACE PROCEDURE update_route_statistics(p_route_id BIGINT)
LANGUAGE plpgsql AS $$
DECLARE
    v_stats RECORD;
BEGIN
    -- Calculate statistics
    SELECT * INTO v_stats
    FROM calculate_route_statistics(p_route_id);
    
    -- Update route statistics table
    INSERT INTO route_statistics (
        route_id,
        total_reservations,
        confirmed_reservations,
        cancelled_reservations,
        total_revenue,
        average_occupancy_rate,
        last_calculated
    )
    VALUES (
        p_route_id,
        v_stats.total_reservations,
        v_stats.confirmed_reservations,
        v_stats.cancelled_reservations,
        v_stats.total_revenue,
        v_stats.average_occupancy_rate,
        CURRENT_TIMESTAMP
    )
    ON CONFLICT (route_id)
    DO UPDATE SET
        total_reservations = EXCLUDED.total_reservations,
        confirmed_reservations = EXCLUDED.confirmed_reservations,
        cancelled_reservations = EXCLUDED.cancelled_reservations,
        total_revenue = EXCLUDED.total_revenue,
        average_occupancy_rate = EXCLUDED.average_occupancy_rate,
        last_calculated = CURRENT_TIMESTAMP;
END;
$$;

-- Procedure to process reservation cancellation
CREATE OR REPLACE PROCEDURE cancel_reservation(
    p_reservation_id BIGINT,
    p_cancelled_by VARCHAR(100),
    p_reason TEXT DEFAULT NULL
)
LANGUAGE plpgsql AS $$
DECLARE
    v_route_id BIGINT;
    v_departure_time TIMESTAMP;
    v_arrival_time TIMESTAMP;
    v_current_status VARCHAR(50);
BEGIN
    -- Get reservation details including departure/arrival times
    SELECT route_id, departure_time, arrival_time, status 
    INTO v_route_id, v_departure_time, v_arrival_time, v_current_status
    FROM reservations
    WHERE id = p_reservation_id;
    
    IF v_route_id IS NULL THEN
        RAISE EXCEPTION 'Reservation % not found', p_reservation_id;
    END IF;
    
    IF v_current_status = 'CANCELLED' THEN
        RAISE EXCEPTION 'Reservation % is already cancelled', p_reservation_id;
    END IF;
    
    IF v_current_status = 'COMPLETED' THEN
        RAISE EXCEPTION 'Cannot cancel a completed reservation (id=%)', p_reservation_id;
    END IF;
    
    -- Update reservation status
    UPDATE reservations
    SET status = 'CANCELLED',
        cancelled_at = CURRENT_TIMESTAMP,
        cancelled_by = p_cancelled_by,
        cancellation_reason = p_reason,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = p_reservation_id;
    
    -- Insert status history
    INSERT INTO reservation_status_history (reservation_id, old_status, new_status, changed_by, change_reason)
    VALUES (p_reservation_id, v_current_status, 'CANCELLED', p_cancelled_by, p_reason);
    
    -- Update route availability for this specific departure time
    CALL update_route_availability(v_route_id, v_departure_time, v_arrival_time);
    
    -- Update route statistics
    CALL update_route_statistics(v_route_id);
END;
$$;

-- Procedure to confirm a reservation
CREATE OR REPLACE PROCEDURE confirm_reservation(
    p_reservation_id BIGINT,
    p_confirmed_by VARCHAR(100)
)
LANGUAGE plpgsql AS $$
DECLARE
    v_route_id BIGINT;
    v_departure_time TIMESTAMP;
    v_arrival_time TIMESTAMP;
    v_current_status VARCHAR(50);
BEGIN
    -- Get reservation details including departure/arrival times
    SELECT route_id, departure_time, arrival_time, status 
    INTO v_route_id, v_departure_time, v_arrival_time, v_current_status
    FROM reservations
    WHERE id = p_reservation_id;
    
    IF v_route_id IS NULL THEN
        RAISE EXCEPTION 'Reservation % not found', p_reservation_id;
    END IF;
    
    IF v_current_status = 'CONFIRMED' THEN
        RAISE EXCEPTION 'Reservation % is already confirmed', p_reservation_id;
    END IF;
    
    IF v_current_status = 'CANCELLED' THEN
        RAISE EXCEPTION 'Cannot confirm a cancelled reservation (id=%)', p_reservation_id;
    END IF;
    
    -- Update reservation status
    UPDATE reservations
    SET status = 'CONFIRMED',
        updated_at = CURRENT_TIMESTAMP,
        updated_by = p_confirmed_by
    WHERE id = p_reservation_id;
    
    -- Insert status history
    INSERT INTO reservation_status_history (reservation_id, old_status, new_status, changed_by)
    VALUES (p_reservation_id, v_current_status, 'CONFIRMED', p_confirmed_by);
    
    -- Update route availability for this specific departure time
    CALL update_route_availability(v_route_id, v_departure_time, v_arrival_time);
    
    -- Update route statistics
    CALL update_route_statistics(v_route_id);
END;
$$;

-- Procedure to generate revenue summary for a date
CREATE OR REPLACE PROCEDURE generate_revenue_summary(p_summary_date DATE)
LANGUAGE plpgsql AS $$
BEGIN
    -- Delete existing summaries for the date
    DELETE FROM revenue_summary
    WHERE summary_date = p_summary_date;
    
    -- Generate summary by route, category, and class
    INSERT INTO revenue_summary (
        summary_date,
        route_id,
        passenger_category,
        vehicle_class,
        reservation_count,
        total_revenue,
        total_vat,
        net_revenue,
        calculated_at
    )
    SELECT 
        p_summary_date,
        route_id,
        passenger_category,
        vehicle_class,
        COUNT(*) AS reservation_count,
        SUM(total_fare) AS total_revenue,
        SUM(vat_amount) AS total_vat,
        SUM(base_fare) AS net_revenue,
        CURRENT_TIMESTAMP
    FROM reservations
    WHERE status = 'CONFIRMED'
      AND DATE(created_at) = p_summary_date
    GROUP BY route_id, passenger_category, vehicle_class;
    
    -- Generate overall summary (NULL for route, category, class)
    INSERT INTO revenue_summary (
        summary_date,
        route_id,
        passenger_category,
        vehicle_class,
        reservation_count,
        total_revenue,
        total_vat,
        net_revenue,
        calculated_at
    )
    SELECT 
        p_summary_date,
        NULL,
        NULL,
        NULL,
        COUNT(*) AS reservation_count,
        SUM(total_fare) AS total_revenue,
        SUM(vat_amount) AS total_vat,
        SUM(base_fare) AS net_revenue,
        CURRENT_TIMESTAMP
    FROM reservations
    WHERE status = 'CONFIRMED'
      AND DATE(created_at) = p_summary_date;
END;
$$;

-- Procedure to initialize route availability for all routes
-- Creates availability entries for all unique departure times from reservations
CREATE OR REPLACE PROCEDURE initialize_all_route_availability()
LANGUAGE plpgsql AS $$
DECLARE
    reservation_record RECORD;
BEGIN
    -- Update availability for each unique (route_id, departure_time) combination
    FOR reservation_record IN 
        SELECT DISTINCT route_id, departure_time, arrival_time
        FROM reservations
        WHERE status IN ('PENDING', 'CONFIRMED')
        ORDER BY route_id, departure_time
    LOOP
        CALL update_route_availability(
            reservation_record.route_id,
            reservation_record.departure_time,
            reservation_record.arrival_time
        );
    END LOOP;
END;
$$;

-- Procedure to initialize route statistics for all routes
CREATE OR REPLACE PROCEDURE initialize_all_route_statistics()
LANGUAGE plpgsql AS $$
DECLARE
    route_record RECORD;
BEGIN
    FOR route_record IN SELECT id FROM routes LOOP
        CALL update_route_statistics(route_record.id);
    END LOOP;
END;
$$;

