-- ============================================================================
-- Transportation Management System - Core Database Schema
-- ============================================================================
-- This script creates the core tables for the Transportation Management System
-- with proper constraints, indexes, and relationships.
-- ============================================================================

-- Drop existing tables in reverse order of dependencies
DROP TABLE IF EXISTS reservation_status_history CASCADE;
DROP TABLE IF EXISTS reservations CASCADE;
DROP TABLE IF EXISTS fare_policy_history CASCADE;
DROP TABLE IF EXISTS fare_policies CASCADE;
DROP TABLE IF EXISTS routes CASCADE;
DROP TABLE IF EXISTS stations CASCADE;
DROP TABLE IF EXISTS vat_rates CASCADE;
DROP TABLE IF EXISTS route_statistics CASCADE;
DROP TABLE IF EXISTS route_availability CASCADE;
DROP TABLE IF EXISTS revenue_summary CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ============================================================================
-- ENUMERATION TABLES
-- ============================================================================

-- Passenger Categories Enum Table (for referential integrity)
CREATE TABLE passenger_categories (
    category VARCHAR(20) PRIMARY KEY,
    description TEXT,
    discount_percentage DECIMAL(5,2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO passenger_categories (category, description, discount_percentage) VALUES
('ADULT', 'Adult passenger (no discount)', 0.00),
('CHILD', 'Child passenger (eligible for discounts)', 25.00),
('SENIOR', 'Senior citizen (eligible for discounts)', 30.00),
('STUDENT', 'Student (eligible for discounts)', 20.00);

-- Vehicle Classes Enum Table
CREATE TABLE vehicle_classes (
    class VARCHAR(20) PRIMARY KEY,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO vehicle_classes (class, description) VALUES
('STANDARD', 'Standard intercity bus'),
('COACH', 'Long-distance, higher-comfort bus'),
('MINI_BUS', 'Smaller van or shuttle'),
('DOUBLE_DECKER', 'Two-level bus');

-- ============================================================================
-- AUTHENTICATION & AUTHORIZATION TABLES
-- ============================================================================

-- Users Table (for authentication and authorization)
-- Uses JPA SINGLE_TABLE inheritance strategy - all user types in one table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    user_type VARCHAR(20) NOT NULL DEFAULT 'USER',  -- Discriminator column for inheritance
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',  -- Kept for backward compatibility
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    CONSTRAINT chk_user_type CHECK (user_type IN ('USER', 'ADMIN')),
    CONSTRAINT chk_user_role CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT chk_username_length CHECK (CHAR_LENGTH(username) >= 3 AND CHAR_LENGTH(username) <= 50),
    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_type ON users(user_type);

COMMENT ON TABLE users IS 'Stores user accounts for authentication and authorization. Uses JPA SINGLE_TABLE inheritance - all user types stored in one table.';
COMMENT ON COLUMN users.user_type IS 'Discriminator column for JPA inheritance: USER (RegularUser) or ADMIN (AdminUser)';
COMMENT ON COLUMN users.role IS 'Role field kept for backward compatibility. USER: Can create reservations. ADMIN: Can manage stations, routes, timetables, and access reports';

-- ============================================================================
-- CORE ENTITY TABLES
-- ============================================================================

-- Stations Table
CREATE TABLE stations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT chk_station_name_length CHECK (CHAR_LENGTH(name) >= 2 AND CHAR_LENGTH(name) <= 100),
    CONSTRAINT chk_station_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'MAINTENANCE', 'CLOSED'))
);

-- Routes Table
CREATE TABLE routes (
    id BIGSERIAL PRIMARY KEY,
    origin_station_id BIGINT NOT NULL,
    destination_station_id BIGINT NOT NULL,
    vehicle_capacity INTEGER NOT NULL DEFAULT 50,
    distance_km DECIMAL(10, 2),
    estimated_duration_minutes INTEGER,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_route_origin FOREIGN KEY (origin_station_id) 
        REFERENCES stations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_route_destination FOREIGN KEY (destination_station_id) 
        REFERENCES stations(id) ON DELETE RESTRICT,
    CONSTRAINT chk_route_capacity CHECK (vehicle_capacity > 0),
    CONSTRAINT chk_route_different_stations CHECK (origin_station_id != destination_station_id),
    CONSTRAINT chk_route_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'DISCONTINUED')),
    CONSTRAINT chk_route_distance CHECK (distance_km IS NULL OR distance_km > 0),
    CONSTRAINT chk_route_duration CHECK (estimated_duration_minutes IS NULL OR estimated_duration_minutes > 0)
);

-- Route Timetables Table
CREATE TABLE route_timetables (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    effective_from DATE NOT NULL,
    effective_to DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_route_timetable_route FOREIGN KEY (route_id)
        REFERENCES routes(id) ON DELETE CASCADE,
    CONSTRAINT chk_route_timetable_dates CHECK (effective_to IS NULL OR effective_to >= effective_from),
    CONSTRAINT chk_route_timetable_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED'))
);

-- Route Timetable Entries Table
CREATE TABLE route_timetable_entries (
    id BIGSERIAL PRIMARY KEY,
    timetable_id BIGINT NOT NULL,
    service_day VARCHAR(10) NOT NULL,
    departure_time TIME NOT NULL,
    arrival_time TIME NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_timetable_entry FOREIGN KEY (timetable_id)
        REFERENCES route_timetables(id) ON DELETE CASCADE,
    CONSTRAINT chk_timetable_service_day CHECK (service_day IN ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')),
    CONSTRAINT chk_timetable_entry_times CHECK (arrival_time > departure_time),
    CONSTRAINT uq_timetable_entry UNIQUE (timetable_id, service_day, departure_time)
);

-- Fare Policies Table (with temporality support)
CREATE TABLE fare_policies (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT NOT NULL,
    passenger_category VARCHAR(20) NOT NULL,
    vehicle_class VARCHAR(20) NOT NULL,
    base_price DECIMAL(10, 2) NOT NULL,
    effective_from TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    effective_to TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_fare_policy_route FOREIGN KEY (route_id) 
        REFERENCES routes(id) ON DELETE CASCADE,
    CONSTRAINT fk_fare_policy_category FOREIGN KEY (passenger_category) 
        REFERENCES passenger_categories(category) ON DELETE RESTRICT,
    CONSTRAINT fk_fare_policy_vehicle_class FOREIGN KEY (vehicle_class) 
        REFERENCES vehicle_classes(class) ON DELETE RESTRICT,
    CONSTRAINT chk_fare_price CHECK (base_price >= 0),
    CONSTRAINT chk_fare_effective_period CHECK (effective_to IS NULL OR effective_to > effective_from),
    CONSTRAINT chk_fare_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'EXPIRED'))
);

-- Reservations Table (with denormalized attributes)
CREATE TABLE reservations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    route_id BIGINT NOT NULL,
    -- Passenger details (optional - defaults to user's info if not provided)
    -- Allows booking for others while maintaining historical accuracy
    passenger_name VARCHAR(100),
    passenger_email VARCHAR(255),
    passenger_phone VARCHAR(20),
    seat_count INTEGER NOT NULL DEFAULT 1,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    passenger_category VARCHAR(20) NOT NULL,
    vehicle_class VARCHAR(20) NOT NULL,
    -- Denormalized fields for performance
    origin_station_name VARCHAR(100),
    destination_station_name VARCHAR(100),
    base_fare DECIMAL(10, 2),
    vat_amount DECIMAL(10, 2),
    total_fare DECIMAL(10, 2),
    -- Trip time details (embedded from TripTimeDetails)
    departure_time TIMESTAMP NOT NULL,
    arrival_time TIMESTAMP NOT NULL,
    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancelled_by VARCHAR(100),
    cancellation_reason TEXT,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_reservation_route FOREIGN KEY (route_id) 
        REFERENCES routes(id) ON DELETE RESTRICT,
    CONSTRAINT fk_reservation_category FOREIGN KEY (passenger_category) 
        REFERENCES passenger_categories(category) ON DELETE RESTRICT,
    CONSTRAINT fk_reservation_vehicle_class FOREIGN KEY (vehicle_class) 
        REFERENCES vehicle_classes(class) ON DELETE RESTRICT,
    CONSTRAINT chk_reservation_seat_count CHECK (seat_count > 0 AND seat_count <= 10),
    CONSTRAINT chk_reservation_status CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW')),
    CONSTRAINT chk_reservation_times CHECK (arrival_time > departure_time),
    CONSTRAINT chk_reservation_future_departure CHECK (departure_time > CURRENT_TIMESTAMP - INTERVAL '1 day'),
    CONSTRAINT chk_reservation_email_format CHECK (passenger_email IS NULL OR passenger_email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- ============================================================================
-- TEMPORALITY TABLES (for historical tracking)
-- ============================================================================

-- VAT Rates Table (supports temporality - VAT can change over time)
CREATE TABLE vat_rates (
    id BIGSERIAL PRIMARY KEY,
    rate_percentage DECIMAL(5, 2) NOT NULL,
    effective_from TIMESTAMP NOT NULL,
    effective_to TIMESTAMP,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    CONSTRAINT chk_vat_rate CHECK (rate_percentage >= 0 AND rate_percentage <= 100),
    CONSTRAINT chk_vat_effective_period CHECK (effective_to IS NULL OR effective_to > effective_from)
);

-- Insert default VAT rate (e.g., 19%)
INSERT INTO vat_rates (rate_percentage, effective_from, description, created_by) 
VALUES (19.00, '2020-01-01 00:00:00', 'Default VAT rate', 'SYSTEM');

-- Fare Policy History Table (audit trail)
CREATE TABLE fare_policy_history (
    id BIGSERIAL PRIMARY KEY,
    fare_policy_id BIGINT NOT NULL,
    route_id BIGINT NOT NULL,
    passenger_category VARCHAR(20) NOT NULL,
    vehicle_class VARCHAR(20) NOT NULL,
    base_price DECIMAL(10, 2) NOT NULL,
    old_price DECIMAL(10, 2),
    change_type VARCHAR(20) NOT NULL, -- 'CREATED', 'UPDATED', 'DELETED', 'ACTIVATED', 'DEACTIVATED'
    effective_from TIMESTAMP NOT NULL,
    effective_to TIMESTAMP,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    changed_by VARCHAR(100),
    change_reason TEXT,
    CONSTRAINT fk_fare_history_policy FOREIGN KEY (fare_policy_id) 
        REFERENCES fare_policies(id) ON DELETE CASCADE,
    CONSTRAINT chk_fare_history_change_type CHECK (change_type IN ('CREATED', 'UPDATED', 'DELETED', 'ACTIVATED', 'DEACTIVATED'))
);

-- Reservation Status History Table (tracks status changes)
CREATE TABLE reservation_status_history (
    id BIGSERIAL PRIMARY KEY,
    reservation_id BIGINT NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    changed_by VARCHAR(100),
    change_reason TEXT,
    CONSTRAINT fk_reservation_history FOREIGN KEY (reservation_id) 
        REFERENCES reservations(id) ON DELETE CASCADE
);

-- ============================================================================
-- DENORMALIZED TABLES (for performance optimization)
-- ============================================================================

-- Route Availability Table (denormalized - stores available seats count per departure time)
-- Tracks availability for each specific trip/departure time on a route
CREATE TABLE route_availability (
    route_id BIGINT NOT NULL,
    departure_time TIMESTAMP NOT NULL,
    arrival_time TIMESTAMP NOT NULL,
    total_capacity INTEGER NOT NULL,
    booked_seats INTEGER NOT NULL DEFAULT 0,
    available_seats INTEGER NOT NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_route_availability FOREIGN KEY (route_id) 
        REFERENCES routes(id) ON DELETE CASCADE,
    CONSTRAINT chk_availability_seats CHECK (booked_seats >= 0 AND available_seats >= 0),
    CONSTRAINT chk_availability_capacity CHECK (booked_seats + available_seats <= total_capacity),
    CONSTRAINT chk_availability_times CHECK (arrival_time > departure_time),
    PRIMARY KEY (route_id, departure_time)
);

-- Route Statistics Table (denormalized - stores aggregated statistics)
CREATE TABLE route_statistics (
    route_id BIGINT PRIMARY KEY,
    total_reservations INTEGER DEFAULT 0,
    confirmed_reservations INTEGER DEFAULT 0,
    cancelled_reservations INTEGER DEFAULT 0,
    total_revenue DECIMAL(15, 2) DEFAULT 0.00,
    average_occupancy_rate DECIMAL(5, 2) DEFAULT 0.00,
    last_calculated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_route_statistics FOREIGN KEY (route_id) 
        REFERENCES routes(id) ON DELETE CASCADE,
    CONSTRAINT chk_statistics_reservations CHECK (total_reservations >= 0),
    CONSTRAINT chk_statistics_revenue CHECK (total_revenue >= 0),
    CONSTRAINT chk_statistics_occupancy CHECK (average_occupancy_rate >= 0 AND average_occupancy_rate <= 100)
);

-- Revenue Summary Table (denormalized - stores daily/monthly revenue summaries)
CREATE TABLE revenue_summary (
    id BIGSERIAL PRIMARY KEY,
    summary_date DATE NOT NULL,
    route_id BIGINT,
    passenger_category VARCHAR(20),
    vehicle_class VARCHAR(20),
    reservation_count INTEGER DEFAULT 0,
    total_revenue DECIMAL(15, 2) DEFAULT 0.00,
    total_vat DECIMAL(15, 2) DEFAULT 0.00,
    net_revenue DECIMAL(15, 2) DEFAULT 0.00,
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_revenue_route FOREIGN KEY (route_id) 
        REFERENCES routes(id) ON DELETE SET NULL,
    CONSTRAINT fk_revenue_category FOREIGN KEY (passenger_category) 
        REFERENCES passenger_categories(category) ON DELETE SET NULL,
    CONSTRAINT fk_revenue_vehicle_class FOREIGN KEY (vehicle_class) 
        REFERENCES vehicle_classes(class) ON DELETE SET NULL,
    CONSTRAINT chk_revenue_summary CHECK (reservation_count >= 0 AND total_revenue >= 0)
);

-- ============================================================================
-- INDEXES (for performance optimization)
-- ============================================================================

-- Stations indexes
CREATE INDEX idx_stations_name ON stations(name);
CREATE INDEX idx_stations_status ON stations(status);

-- Routes indexes
CREATE INDEX idx_routes_origin ON routes(origin_station_id);
CREATE INDEX idx_routes_destination ON routes(destination_station_id);
CREATE INDEX idx_routes_status ON routes(status);
CREATE INDEX idx_routes_origin_destination ON routes(origin_station_id, destination_station_id);
CREATE INDEX idx_route_timetables_route ON route_timetables(route_id);
CREATE INDEX idx_route_timetables_status ON route_timetables(status);
CREATE INDEX idx_route_timetable_entries_timetable ON route_timetable_entries(timetable_id);
CREATE INDEX idx_route_timetable_entries_day ON route_timetable_entries(service_day);

-- Fare Policies indexes
CREATE INDEX idx_fare_policies_route ON fare_policies(route_id);
CREATE INDEX idx_fare_policies_category ON fare_policies(passenger_category);
CREATE INDEX idx_fare_policies_vehicle_class ON fare_policies(vehicle_class);
CREATE INDEX idx_fare_policies_effective ON fare_policies(effective_from, effective_to);
CREATE INDEX idx_fare_policies_status ON fare_policies(status);
CREATE INDEX idx_fare_policies_lookup ON fare_policies(route_id, passenger_category, vehicle_class, status, effective_from, effective_to);

-- Partial unique index: Only one active fare policy with no end date per route/category/class
-- Note: The trigger validate_fare_policy_dates handles overlapping policies more comprehensively
CREATE UNIQUE INDEX idx_fare_policies_active_unique 
ON fare_policies(route_id, passenger_category, vehicle_class) 
WHERE status = 'ACTIVE' AND effective_to IS NULL;

-- Reservations indexes
CREATE INDEX idx_reservations_user ON reservations(user_id);
CREATE INDEX idx_reservations_route ON reservations(route_id);
CREATE INDEX idx_reservations_status ON reservations(status);
CREATE INDEX idx_reservations_user_status ON reservations(user_id, status);
CREATE INDEX idx_reservations_passenger_name ON reservations(passenger_name);
CREATE INDEX idx_reservations_email ON reservations(passenger_email);
CREATE INDEX idx_reservations_departure_time ON reservations(departure_time);
CREATE INDEX idx_reservations_created_at ON reservations(created_at);
CREATE INDEX idx_reservations_route_status ON reservations(route_id, status);
CREATE INDEX idx_reservations_departure_range ON reservations(departure_time, arrival_time);

-- VAT Rates indexes
CREATE INDEX idx_vat_rates_effective ON vat_rates(effective_from, effective_to);
-- Note: Overlapping VAT rates are prevented by the trigger validate_vat_rate_dates

-- Route Availability indexes
CREATE INDEX idx_route_availability_available ON route_availability(available_seats) WHERE available_seats > 0;
CREATE INDEX idx_route_availability_route_departure ON route_availability(route_id, departure_time);
CREATE INDEX idx_route_availability_departure ON route_availability(departure_time);

-- Revenue Summary indexes
CREATE INDEX idx_revenue_summary_date ON revenue_summary(summary_date);
CREATE INDEX idx_revenue_summary_route ON revenue_summary(route_id);
CREATE INDEX idx_revenue_summary_date_route ON revenue_summary(summary_date, route_id);

-- ============================================================================
-- COMMENTS (documentation)
-- ============================================================================

COMMENT ON TABLE stations IS 'Stores information about transportation stations';
COMMENT ON TABLE routes IS 'Stores routes between stations with capacity information';
COMMENT ON TABLE fare_policies IS 'Stores fare pricing policies with temporal support (effective dates)';
COMMENT ON TABLE reservations IS 'Stores passenger reservations with denormalized fare information. Linked to users table to track who made each reservation.';
COMMENT ON COLUMN reservations.user_id IS 'Foreign key to users table. Identifies which user account created this reservation.';
COMMENT ON COLUMN reservations.passenger_name IS 'Optional: Name of the passenger. If NULL, defaults to user''s name (first_name + last_name). Allows booking for others.';
COMMENT ON COLUMN reservations.passenger_email IS 'Optional: Email of the passenger. If NULL, defaults to user''s email. Maintains historical accuracy.';
COMMENT ON COLUMN reservations.passenger_phone IS 'Optional: Phone of the passenger. If NULL, defaults to user''s phone.';
COMMENT ON TABLE route_timetables IS 'Stores named timetables/schedules for specific routes';
COMMENT ON TABLE route_timetable_entries IS 'Stores individual timetable entries per day of week for a route timetable';
COMMENT ON TABLE vat_rates IS 'Stores VAT rates with temporal support (tracks VAT changes over time)';
COMMENT ON TABLE route_availability IS 'Denormalized table storing current seat availability per departure time for fast queries. Uses time-aware overlap checking via calculate_booked_seats().';
COMMENT ON TABLE route_statistics IS 'Denormalized table storing aggregated route statistics';
COMMENT ON TABLE revenue_summary IS 'Denormalized table storing revenue summaries by date, route, category, and class';
COMMENT ON TABLE fare_policy_history IS 'Audit trail for fare policy changes';
COMMENT ON TABLE reservation_status_history IS 'Audit trail for reservation status changes';

COMMENT ON COLUMN reservations.origin_station_name IS 'Denormalized: cached origin station name for performance';
COMMENT ON COLUMN reservations.destination_station_name IS 'Denormalized: cached destination station name for performance';
COMMENT ON COLUMN reservations.base_fare IS 'Denormalized: cached base fare at time of reservation';
COMMENT ON COLUMN reservations.vat_amount IS 'Denormalized: cached VAT amount at time of reservation';
COMMENT ON COLUMN reservations.total_fare IS 'Denormalized: cached total fare (base + VAT) at time of reservation';

