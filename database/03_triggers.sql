-- ============================================================================
-- Transportation Management System - Triggers
-- ============================================================================
-- This script creates triggers for:
-- 1. Complex validation rules
-- 2. Updating calculated/denormalized attributes
-- 3. Maintaining audit trails
-- ============================================================================

-- ============================================================================
-- TRIGGER FUNCTIONS FOR VALIDATION
-- ============================================================================

-- Function to validate route creation (prevent circular routes)
CREATE OR REPLACE FUNCTION validate_route_creation()
RETURNS TRIGGER AS $$
BEGIN
    -- Check if origin and destination are different
    IF NEW.origin_station_id = NEW.destination_station_id THEN
        RAISE EXCEPTION 'Route cannot have the same origin and destination station (circular route not allowed)';
    END IF;
    
    -- Check if route already exists (optional: allow multiple routes between same stations)
    -- This is commented out as business logic might allow multiple routes
    /*
    IF EXISTS (
        SELECT 1 FROM routes
        WHERE origin_station_id = NEW.origin_station_id
          AND destination_station_id = NEW.destination_station_id
          AND id != NEW.id
          AND status = 'ACTIVE'
    ) THEN
        RAISE EXCEPTION 'Active route between these stations already exists';
    END IF;
    */
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function to validate reservation times
CREATE OR REPLACE FUNCTION validate_reservation_times()
RETURNS TRIGGER AS $$
BEGIN
    -- Check if arrival time is after departure time
    IF NEW.arrival_time <= NEW.departure_time THEN
        RAISE EXCEPTION 'Arrival time must be after departure time';
    END IF;
    
    -- Check if departure time is not too far in the past (allow 1 day grace period for corrections)
    IF NEW.departure_time < (CURRENT_TIMESTAMP - INTERVAL '1 day') THEN
        RAISE EXCEPTION 'Departure time cannot be more than 1 day in the past';
    END IF;
    
    -- Check if departure time is not too far in the future (e.g., 1 year)
    IF NEW.departure_time > (CURRENT_TIMESTAMP + INTERVAL '1 year') THEN
        RAISE EXCEPTION 'Departure time cannot be more than 1 year in the future';
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function to validate seat capacity
CREATE OR REPLACE FUNCTION validate_seat_capacity()
RETURNS TRIGGER AS $$
DECLARE
    v_available_seats INTEGER;
    v_route_capacity INTEGER;
    v_current_booked INTEGER;
BEGIN
    -- Get route capacity
    SELECT vehicle_capacity INTO v_route_capacity
    FROM routes
    WHERE id = NEW.route_id;
    
    IF v_route_capacity IS NULL THEN
        RAISE EXCEPTION 'Route % not found', NEW.route_id;
    END IF;
    
    -- If status is being changed to CANCELLED, allow it (seats will be freed)
    IF NEW.status = 'CANCELLED' THEN
        RETURN NEW;
    END IF;
    
    -- For new reservations or status changes to CONFIRMED/PENDING
    IF TG_OP = 'INSERT' OR (TG_OP = 'UPDATE' AND NEW.status IN ('PENDING', 'CONFIRMED')) THEN
        -- Calculate currently booked seats for overlapping time periods
        -- Two reservations overlap if: new.departure < existing.arrival AND new.arrival > existing.departure
        SELECT COALESCE(SUM(seat_count), 0) INTO v_current_booked
        FROM reservations
        WHERE route_id = NEW.route_id
          AND status IN ('PENDING', 'CONFIRMED')
          AND (TG_OP = 'INSERT' OR id != NEW.id)
          AND departure_time < NEW.arrival_time
          AND arrival_time > NEW.departure_time;
        
        -- Check if adding this reservation would exceed capacity
        IF (v_current_booked + NEW.seat_count) > v_route_capacity THEN
            RAISE EXCEPTION 'Insufficient capacity: Requested % seats, but only % seats available (capacity: %)',
                NEW.seat_count,
                (v_route_capacity - v_current_booked),
                v_route_capacity;
        END IF;
        
        -- Check seat count limits
        IF NEW.seat_count <= 0 THEN
            RAISE EXCEPTION 'Seat count must be greater than 0';
        END IF;
        
        IF NEW.seat_count > 10 THEN
            RAISE EXCEPTION 'Maximum 10 seats can be booked per reservation';
        END IF;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function to validate fare policy effective dates
CREATE OR REPLACE FUNCTION validate_fare_policy_dates()
RETURNS TRIGGER AS $$
BEGIN
    -- Check if effective_to is after effective_from
    IF NEW.effective_to IS NOT NULL AND NEW.effective_to <= NEW.effective_from THEN
        RAISE EXCEPTION 'Effective end date must be after effective start date';
    END IF;
    
    -- Check for overlapping active policies (only one active policy per route/category/class at a time)
    IF NEW.status = 'ACTIVE' THEN
        IF EXISTS (
            SELECT 1 FROM fare_policies
            WHERE route_id = NEW.route_id
              AND passenger_category = NEW.passenger_category
              AND vehicle_class = NEW.vehicle_class
              AND status = 'ACTIVE'
              AND id != NEW.id
              AND (
                  (effective_to IS NULL AND NEW.effective_to IS NULL) OR
                  (effective_to IS NULL AND NEW.effective_from <= CURRENT_TIMESTAMP) OR
                  (NEW.effective_to IS NULL AND effective_from <= CURRENT_TIMESTAMP) OR
                  (NEW.effective_from <= COALESCE(effective_to, '9999-12-31'::TIMESTAMP) AND
                   COALESCE(NEW.effective_to, '9999-12-31'::TIMESTAMP) >= effective_from)
              )
        ) THEN
            RAISE EXCEPTION 'Overlapping active fare policy exists for route %, category %, class %',
                NEW.route_id, NEW.passenger_category, NEW.vehicle_class;
        END IF;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function to validate VAT rate effective dates
CREATE OR REPLACE FUNCTION validate_vat_rate_dates()
RETURNS TRIGGER AS $$
BEGIN
    -- Check if effective_to is after effective_from
    IF NEW.effective_to IS NOT NULL AND NEW.effective_to <= NEW.effective_from THEN
        RAISE EXCEPTION 'Effective end date must be after effective start date';
    END IF;
    
    -- Check for overlapping VAT rates (only one active VAT rate at a time)
    IF EXISTS (
        SELECT 1 FROM vat_rates
        WHERE id != NEW.id
          AND (
              (effective_to IS NULL AND NEW.effective_to IS NULL) OR
              (effective_to IS NULL AND NEW.effective_from <= CURRENT_TIMESTAMP) OR
              (NEW.effective_to IS NULL AND effective_from <= CURRENT_TIMESTAMP) OR
              (NEW.effective_from <= COALESCE(effective_to, '9999-12-31'::TIMESTAMP) AND
               COALESCE(NEW.effective_to, '9999-12-31'::TIMESTAMP) >= effective_from)
          )
    ) THEN
        RAISE EXCEPTION 'Overlapping VAT rate exists for the specified date range';
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function to validate station status changes
CREATE OR REPLACE FUNCTION validate_station_status()
RETURNS TRIGGER AS $$
BEGIN
    -- Prevent setting station to CLOSED if it has active routes
    IF NEW.status = 'CLOSED' AND OLD.status != 'CLOSED' THEN
        IF EXISTS (
            SELECT 1 FROM routes
            WHERE (origin_station_id = NEW.id OR destination_station_id = NEW.id)
              AND status = 'ACTIVE'
        ) THEN
            RAISE EXCEPTION 'Cannot close station % because it has active routes', NEW.name;
        END IF;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Utility function: update timestamp fields
CREATE OR REPLACE FUNCTION touch_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at := CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- TRIGGER FUNCTIONS FOR DENORMALIZATION
-- ============================================================================

-- Function to update denormalized reservation fields
CREATE OR REPLACE FUNCTION update_reservation_denormalized_fields()
RETURNS TRIGGER AS $$
DECLARE
    v_origin_name VARCHAR(100);
    v_destination_name VARCHAR(100);
    v_base_price DECIMAL(10, 2);
    v_fare_calc RECORD;
BEGIN
    -- Only calculate if this is a new reservation or route/passenger/vehicle changed
    IF TG_OP = 'INSERT' OR 
       (TG_OP = 'UPDATE' AND (
           OLD.route_id != NEW.route_id OR
           OLD.passenger_category != NEW.passenger_category OR
           OLD.vehicle_class != NEW.vehicle_class OR
           OLD.seat_count != NEW.seat_count
       )) THEN
        
        -- Get station names
        SELECT 
            origin.name,
            destination.name
        INTO 
            v_origin_name,
            v_destination_name
        FROM routes r
        JOIN stations origin ON r.origin_station_id = origin.id
        JOIN stations destination ON r.destination_station_id = destination.id
        WHERE r.id = NEW.route_id;
        
        -- Get base price (use reservation creation time or departure time)
        BEGIN
            v_base_price := get_active_fare_policy(
                NEW.route_id,
                NEW.passenger_category,
                NEW.vehicle_class,
                COALESCE(NEW.departure_time, CURRENT_TIMESTAMP)
            );
        EXCEPTION
            WHEN OTHERS THEN
                -- If no fare policy found, set to 0
                v_base_price := 0.00;
        END;
        
        -- Calculate fare with VAT
        SELECT * INTO v_fare_calc
        FROM calculate_fare_with_vat(
            v_base_price,
            NEW.seat_count,
            get_vat_rate_for_date(COALESCE(NEW.departure_time, CURRENT_TIMESTAMP))
        );
        
        -- Update denormalized fields
        NEW.origin_station_name := v_origin_name;
        NEW.destination_station_name := v_destination_name;
        NEW.base_fare := v_fare_calc.base_fare;
        NEW.vat_amount := v_fare_calc.vat_amount;
        NEW.total_fare := v_fare_calc.total_fare;
    END IF;
    
    -- Update updated_at timestamp
    IF TG_OP = 'UPDATE' THEN
        NEW.updated_at := CURRENT_TIMESTAMP;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function to update route availability after reservation changes
CREATE OR REPLACE FUNCTION update_route_availability_trigger()
RETURNS TRIGGER AS $$
DECLARE
    v_route_id BIGINT;
BEGIN
    -- Determine which route to update
    IF TG_OP = 'DELETE' THEN
        v_route_id := OLD.route_id;
    ELSE
        v_route_id := NEW.route_id;
    END IF;
    
    -- Update route availability asynchronously (using a scheduled job would be better)
    -- For now, we'll update it directly
    CALL update_route_availability(v_route_id);
    
    -- Update route statistics
    CALL update_route_statistics(v_route_id);
    
    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    ELSE
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Function to maintain reservation status history
CREATE OR REPLACE FUNCTION maintain_reservation_status_history()
RETURNS TRIGGER AS $$
BEGIN
    -- Only insert history if status changed
    IF TG_OP = 'UPDATE' AND OLD.status != NEW.status THEN
        INSERT INTO reservation_status_history (
            reservation_id,
            old_status,
            new_status,
            changed_at,
            changed_by,
            change_reason
        )
        VALUES (
            NEW.id,
            OLD.status,
            NEW.status,
            CURRENT_TIMESTAMP,
            NEW.updated_by,
            CASE 
                WHEN NEW.status = 'CANCELLED' THEN NEW.cancellation_reason
                ELSE NULL
            END
        );
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function to maintain fare policy history
CREATE OR REPLACE FUNCTION maintain_fare_policy_history()
RETURNS TRIGGER AS $$
DECLARE
    v_change_type VARCHAR(20);
BEGIN
    IF TG_OP = 'INSERT' THEN
        v_change_type := 'CREATED';
        INSERT INTO fare_policy_history (
            fare_policy_id,
            route_id,
            passenger_category,
            vehicle_class,
            base_price,
            old_price,
            change_type,
            effective_from,
            effective_to,
            changed_at,
            changed_by
        )
        VALUES (
            NEW.id,
            NEW.route_id,
            NEW.passenger_category,
            NEW.vehicle_class,
            NEW.base_price,
            NULL,
            v_change_type,
            NEW.effective_from,
            NEW.effective_to,
            CURRENT_TIMESTAMP,
            NEW.created_by
        );
    ELSIF TG_OP = 'UPDATE' THEN
        -- Determine change type
        IF OLD.status != NEW.status THEN
            IF NEW.status = 'ACTIVE' THEN
                v_change_type := 'ACTIVATED';
            ELSIF NEW.status = 'INACTIVE' THEN
                v_change_type := 'DEACTIVATED';
            END IF;
        ELSE
            v_change_type := 'UPDATED';
        END IF;
        
        -- Only record if there was a significant change
        IF OLD.base_price != NEW.base_price OR 
           OLD.status != NEW.status OR
           OLD.effective_from != NEW.effective_from OR
           OLD.effective_to != NEW.effective_to THEN
            
            INSERT INTO fare_policy_history (
                fare_policy_id,
                route_id,
                passenger_category,
                vehicle_class,
                base_price,
                old_price,
                change_type,
                effective_from,
                effective_to,
                changed_at,
                changed_by
            )
            VALUES (
                NEW.id,
                NEW.route_id,
                NEW.passenger_category,
                NEW.vehicle_class,
                NEW.base_price,
                OLD.base_price,
                v_change_type,
                NEW.effective_from,
                NEW.effective_to,
                CURRENT_TIMESTAMP,
                NEW.updated_by
            );
        END IF;
    ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO fare_policy_history (
            fare_policy_id,
            route_id,
            passenger_category,
            vehicle_class,
            base_price,
            old_price,
            change_type,
            effective_from,
            effective_to,
            changed_at,
            changed_by
        )
        VALUES (
            OLD.id,
            OLD.route_id,
            OLD.passenger_category,
            OLD.vehicle_class,
            NULL,
            OLD.base_price,
            'DELETED',
            OLD.effective_from,
            OLD.effective_to,
            CURRENT_TIMESTAMP,
            'SYSTEM'
        );
    END IF;
    
    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    ELSE
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Function to update route statistics when route changes
CREATE OR REPLACE FUNCTION update_route_on_change()
RETURNS TRIGGER AS $$
BEGIN
    -- Update updated_at timestamp
    NEW.updated_at := CURRENT_TIMESTAMP;
    
    -- If capacity changed, update availability
    IF TG_OP = 'UPDATE' AND OLD.vehicle_capacity != NEW.vehicle_capacity THEN
        CALL update_route_availability(NEW.id);
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function to update station on change
CREATE OR REPLACE FUNCTION update_station_on_change()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at := CURRENT_TIMESTAMP;
    
    -- If station name changed, update denormalized fields in reservations
    IF TG_OP = 'UPDATE' AND OLD.name != NEW.name THEN
        UPDATE reservations
        SET origin_station_name = NEW.name
        WHERE route_id IN (
            SELECT id FROM routes WHERE origin_station_id = NEW.id
        );
        
        UPDATE reservations
        SET destination_station_name = NEW.name
        WHERE route_id IN (
            SELECT id FROM routes WHERE destination_station_id = NEW.id
        );
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- VALIDATION TRIGGERS
-- ============================================================================

-- Trigger to validate route creation
CREATE TRIGGER trigger_validate_route_creation
    BEFORE INSERT OR UPDATE ON routes
    FOR EACH ROW
    EXECUTE FUNCTION validate_route_creation();

-- Trigger to validate reservation times
CREATE TRIGGER trigger_validate_reservation_times
    BEFORE INSERT OR UPDATE ON reservations
    FOR EACH ROW
    EXECUTE FUNCTION validate_reservation_times();

-- Trigger to validate seat capacity
CREATE TRIGGER trigger_validate_seat_capacity
    BEFORE INSERT OR UPDATE ON reservations
    FOR EACH ROW
    EXECUTE FUNCTION validate_seat_capacity();

-- Trigger to validate fare policy dates
CREATE TRIGGER trigger_validate_fare_policy_dates
    BEFORE INSERT OR UPDATE ON fare_policies
    FOR EACH ROW
    EXECUTE FUNCTION validate_fare_policy_dates();

-- Trigger to validate VAT rate dates
CREATE TRIGGER trigger_validate_vat_rate_dates
    BEFORE INSERT OR UPDATE ON vat_rates
    FOR EACH ROW
    EXECUTE FUNCTION validate_vat_rate_dates();

-- Trigger to validate station status
CREATE TRIGGER trigger_validate_station_status
    BEFORE UPDATE ON stations
    FOR EACH ROW
    EXECUTE FUNCTION validate_station_status();

-- ============================================================================
-- DENORMALIZATION TRIGGERS
-- ============================================================================

-- Trigger to update denormalized reservation fields
CREATE TRIGGER trigger_update_reservation_denormalized
    BEFORE INSERT OR UPDATE ON reservations
    FOR EACH ROW
    EXECUTE FUNCTION update_reservation_denormalized_fields();

-- Trigger to update route availability
CREATE TRIGGER trigger_update_route_availability
    AFTER INSERT OR UPDATE OR DELETE ON reservations
    FOR EACH ROW
    EXECUTE FUNCTION update_route_availability_trigger();

-- Trigger to maintain reservation status history
CREATE TRIGGER trigger_maintain_reservation_status_history
    AFTER UPDATE ON reservations
    FOR EACH ROW
    EXECUTE FUNCTION maintain_reservation_status_history();

-- Trigger to maintain fare policy history
CREATE TRIGGER trigger_maintain_fare_policy_history
    AFTER INSERT OR UPDATE OR DELETE ON fare_policies
    FOR EACH ROW
    EXECUTE FUNCTION maintain_fare_policy_history();

-- Trigger to update route on change
CREATE TRIGGER trigger_update_route_on_change
    BEFORE UPDATE ON routes
    FOR EACH ROW
    EXECUTE FUNCTION update_route_on_change();

-- Trigger to update station on change
CREATE TRIGGER trigger_update_station_on_change
    BEFORE UPDATE ON stations
    FOR EACH ROW
    EXECUTE FUNCTION update_station_on_change();

-- Trigger to update route timetable timestamp
CREATE TRIGGER trigger_update_route_timetable_timestamp
    BEFORE UPDATE ON route_timetables
    FOR EACH ROW
    EXECUTE FUNCTION touch_updated_at();

-- Trigger to update route timetable entry timestamp
CREATE TRIGGER trigger_update_route_timetable_entry_timestamp
    BEFORE UPDATE ON route_timetable_entries
    FOR EACH ROW
    EXECUTE FUNCTION touch_updated_at();

-- ============================================================================
-- ADDITIONAL TRIGGERS FOR SPECIAL CASES
-- ============================================================================

-- Function to prevent deletion of stations with active routes
CREATE OR REPLACE FUNCTION prevent_station_deletion()
RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM routes
        WHERE origin_station_id = OLD.id OR destination_station_id = OLD.id
    ) THEN
        RAISE EXCEPTION 'Cannot delete station % because it is used in routes', OLD.name;
    END IF;
    
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_prevent_station_deletion
    BEFORE DELETE ON stations
    FOR EACH ROW
    EXECUTE FUNCTION prevent_station_deletion();

-- Function to prevent deletion of routes with reservations
CREATE OR REPLACE FUNCTION prevent_route_deletion()
RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM reservations
        WHERE route_id = OLD.id
    ) THEN
        RAISE EXCEPTION 'Cannot delete route % because it has reservations', OLD.id;
    END IF;
    
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_prevent_route_deletion
    BEFORE DELETE ON routes
    FOR EACH ROW
    EXECUTE FUNCTION prevent_route_deletion();

-- Function to automatically create route availability when route is created
CREATE OR REPLACE FUNCTION create_route_availability()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO route_availability (route_id, total_capacity, booked_seats, available_seats, last_updated)
    VALUES (NEW.id, NEW.vehicle_capacity, 0, NEW.vehicle_capacity, CURRENT_TIMESTAMP)
    ON CONFLICT (route_id) DO NOTHING;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_create_route_availability
    AFTER INSERT ON routes
    FOR EACH ROW
    EXECUTE FUNCTION create_route_availability();

-- Function to automatically create route statistics when route is created
CREATE OR REPLACE FUNCTION create_route_statistics()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO route_statistics (route_id, total_reservations, confirmed_reservations, cancelled_reservations, total_revenue, average_occupancy_rate, last_calculated)
    VALUES (NEW.id, 0, 0, 0, 0.00, 0.00, CURRENT_TIMESTAMP)
    ON CONFLICT (route_id) DO NOTHING;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_create_route_statistics
    AFTER INSERT ON routes
    FOR EACH ROW
    EXECUTE FUNCTION create_route_statistics();

