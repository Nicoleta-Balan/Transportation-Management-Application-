# Transportation Management System - Database Schema Documentation

## Table of Contents

1. [Overview](#overview)
2. [Database Architecture](#database-architecture)
3. [Schema Design](#schema-design)
4. [Denormalization Strategy](#denormalization-strategy)
5. [Temporality Implementation](#temporality-implementation)
6. [Functions and Procedures](#functions-and-procedures)
7. [Triggers](#triggers)
8. [Special Cases and Exceptions](#special-cases-and-exceptions)
9. [Installation and Setup](#installation-and-setup)
10. [Usage Examples](#usage-examples)
11. [Maintenance and Best Practices](#maintenance-and-best-practices)

---

## Overview

This database schema is designed for a Transportation Management System that handles:
- **Station Management**: Managing transportation stations
- **Route Management**: Managing routes between stations
- **Fare Policy Management**: Managing pricing policies with temporal support
- **Reservation Management**: Handling passenger reservations
- **Financial Management**: Tracking revenue, VAT, and financial metrics
- **Statistics and Analytics**: Aggregated statistics for reporting

### Key Features

- ✅ **Comprehensive Validation**: Complex business rules enforced at database level
- ✅ **Denormalization**: Performance-optimized with calculated and cached attributes
- ✅ **Temporality**: Historical tracking of VAT rates and fare policies
- ✅ **Audit Trails**: Complete history of changes to critical entities
- ✅ **Data Integrity**: Foreign keys, check constraints, and triggers ensure data consistency
- ✅ **Performance Optimization**: Indexes, denormalized tables, and efficient queries

---

## Database Architecture

### Technology Stack

- **Database**: PostgreSQL 15+
- **Language**: PL/pgSQL for stored procedures and triggers
- **Features Used**: 
  - Triggers (BEFORE/AFTER)
  - Stored Procedures
  - Functions
  - Check Constraints
  - Foreign Keys
  - Indexes
  - Unique Constraints with NULL handling

### Database Structure

```
Transportation Management Database
├── Core Tables
│   ├── stations
│   ├── routes
│   ├── fare_policies
│   └── reservations
├── Enumeration Tables
│   ├── passenger_categories
│   └── vehicle_classes
├── Temporality Tables
│   ├── vat_rates
│   ├── fare_policy_history
│   └── reservation_status_history
├── Denormalized Tables
│   ├── route_availability
│   ├── route_statistics
│   └── revenue_summary
└── Supporting Objects
    ├── Functions
    ├── Procedures
    └── Triggers
```

---

## Schema Design

### Core Tables

#### 1. Stations Table

**Purpose**: Stores information about transportation stations.

**Key Columns**:
- `id`: Primary key
- `name`: Unique station name
- `status`: Station status (ACTIVE, INACTIVE, MAINTENANCE, CLOSED)
- `latitude`, `longitude`: Geographic coordinates
- `address`: Physical address

**Constraints**:
- Unique station names
- Status must be one of: ACTIVE, INACTIVE, MAINTENANCE, CLOSED
- Name length between 2-100 characters

**Special Features**:
- Cannot be deleted if used in routes
- Status change validation (cannot close if active routes exist)

#### 2. Routes Table

**Purpose**: Stores routes between stations.

**Key Columns**:
- `id`: Primary key
- `origin_station_id`: Foreign key to stations
- `destination_station_id`: Foreign key to stations
- `vehicle_capacity`: Maximum number of seats
- `distance_km`: Route distance in kilometers
- `estimated_duration_minutes`: Estimated travel time

**Constraints**:
- Origin and destination must be different (no circular routes)
- Capacity must be greater than 0
- Status validation

**Special Features**:
- Prevents circular routes (origin = destination)
- Cannot be deleted if reservations exist
- Automatic creation of availability and statistics records

#### 3. Fare Policies Table

**Purpose**: Stores pricing policies with temporal support.

**Key Columns**:
- `id`: Primary key
- `route_id`: Foreign key to routes
- `passenger_category`: ADULT, CHILD, SENIOR, STUDENT
- `vehicle_class`: STANDARD, COACH, MINI_BUS, DOUBLE_DECKER
- `base_price`: Base fare price
- `effective_from`: Start date of policy
- `effective_to`: End date of policy (NULL for active policies)
- `status`: ACTIVE, INACTIVE, EXPIRED

**Constraints**:
- Unique combination of route, category, class, and effective period
- No overlapping active policies
- Effective dates validation

**Special Features**:
- Temporal support (tracks fare changes over time)
- Automatic history tracking
- Prevents overlapping active policies

#### 4. Reservations Table

**Purpose**: Stores passenger reservations.

**Key Columns**:
- `id`: Primary key
- `route_id`: Foreign key to routes
- `passenger_name`: Passenger name
- `passenger_email`: Email address (validated)
- `passenger_phone`: Phone number
- `seat_count`: Number of seats
- `status`: PENDING, CONFIRMED, CANCELLED, COMPLETED, NO_SHOW
- `passenger_category`: Passenger category
- `vehicle_class`: Vehicle class
- **Denormalized Fields**:
  - `origin_station_name`: Cached origin station name
  - `destination_station_name`: Cached destination station name
  - `base_fare`: Cached base fare at reservation time
  - `vat_amount`: Cached VAT amount at reservation time
  - `total_fare`: Cached total fare at reservation time
- `departure_time`: Trip departure time
- `arrival_time`: Trip arrival time

**Constraints**:
- Seat count between 1-10
- Arrival time must be after departure time
- Departure time cannot be more than 1 day in the past
- Email format validation
- Status validation

**Special Features**:
- Automatic fare calculation on insert/update
- Denormalized fields for performance
- Status history tracking
- Capacity validation

### Enumeration Tables

#### Passenger Categories

- **ADULT**: No discount (0%)
- **CHILD**: 25% discount
- **SENIOR**: 30% discount
- **STUDENT**: 20% discount

#### Vehicle Classes

- **STANDARD**: Standard intercity bus
- **COACH**: Long-distance, higher-comfort bus
- **MINI_BUS**: Smaller van or shuttle
- **DOUBLE_DECKER**: Two-level bus

---

## Denormalization Strategy

### Rationale

Denormalization is used to improve query performance by:
1. **Reducing JOIN operations**: Cached values eliminate need for joins
2. **Faster aggregations**: Pre-calculated statistics
3. **Real-time availability**: Instant seat availability checks
4. **Historical accuracy**: Preserves pricing at time of reservation

### Denormalized Tables

#### 1. Route Availability Table

**Purpose**: Stores current seat availability for fast queries.

**Columns**:
- `route_id`: Primary key (foreign key to routes)
- `total_capacity`: Total vehicle capacity
- `booked_seats`: Number of booked seats
- `available_seats`: Available seats (calculated)
- `last_updated`: Timestamp of last update

**Update Strategy**:
- Automatically updated via triggers when reservations are created/updated/deleted
- Can be manually refreshed using `update_route_availability()` procedure

**Benefits**:
- O(1) availability checks (no need to count reservations)
- Prevents overbooking
- Fast queries for available routes

#### 2. Route Statistics Table

**Purpose**: Stores aggregated route statistics.

**Columns**:
- `route_id`: Primary key
- `total_reservations`: Total number of reservations
- `confirmed_reservations`: Number of confirmed reservations
- `cancelled_reservations`: Number of cancelled reservations
- `total_revenue`: Total revenue from confirmed reservations
- `average_occupancy_rate`: Average occupancy percentage
- `last_calculated`: Timestamp of last calculation

**Update Strategy**:
- Automatically updated via triggers
- Can be manually refreshed using `update_route_statistics()` procedure

**Benefits**:
- Fast reporting without aggregating reservations
- Historical tracking of route performance
- Efficient dashboard queries

#### 3. Revenue Summary Table

**Purpose**: Stores daily revenue summaries.

**Columns**:
- `id`: Primary key
- `summary_date`: Date of summary
- `route_id`: Route (NULL for overall summary)
- `passenger_category`: Category (NULL for overall summary)
- `vehicle_class`: Class (NULL for overall summary)
- `reservation_count`: Number of reservations
- `total_revenue`: Total revenue
- `total_vat`: Total VAT
- `net_revenue`: Net revenue (before VAT)

**Update Strategy**:
- Manually generated using `generate_revenue_summary()` procedure
- Can be scheduled to run daily

**Benefits**:
- Fast historical revenue queries
- Multi-dimensional analysis (by route, category, class)
- Efficient reporting

### Denormalized Fields in Reservations

#### Cached Station Names
- `origin_station_name`: Cached for performance
- `destination_station_name`: Cached for performance
- **Update**: Automatically updated when reservation is created or route changes

#### Cached Fare Information
- `base_fare`: Base fare at time of reservation
- `vat_amount`: VAT amount at time of reservation
- `total_fare`: Total fare at time of reservation
- **Rationale**: Preserves pricing even if fare policies or VAT rates change
- **Update**: Automatically calculated on insert/update

---

## Temporality Implementation

### VAT Rates Temporality

**Problem**: VAT rates can change over time, and we need to:
1. Track historical VAT rates
2. Calculate VAT for reservations using the rate effective at reservation time
3. Generate reports with correct historical VAT amounts

**Solution**: `vat_rates` table with effective dates

```sql
CREATE TABLE vat_rates (
    id BIGSERIAL PRIMARY KEY,
    rate_percentage DECIMAL(5, 2) NOT NULL,
    effective_from TIMESTAMP NOT NULL,
    effective_to TIMESTAMP,  -- NULL for current rate
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Features**:
- `effective_from`: Start date of VAT rate
- `effective_to`: End date (NULL for current rate)
- Prevents overlapping rates
- Functions to get VAT rate for specific dates

**Usage**:
```sql
-- Get current VAT rate
SELECT get_current_vat_rate();

-- Get VAT rate for a specific date
SELECT get_vat_rate_for_date('2024-01-15 10:00:00'::TIMESTAMP);
```

### Fare Policy Temporality

**Problem**: Fare policies can change over time, and we need to:
1. Track historical fare changes
2. Preserve fare at time of reservation
3. Support future-dated fare changes

**Solution**: `fare_policies` table with effective dates and history table

**Features**:
- `effective_from`: Start date of policy
- `effective_to`: End date (NULL for active policy)
- `fare_policy_history`: Audit trail of all changes
- Prevents overlapping active policies
- Supports future-dated changes

**Usage**:
```sql
-- Get active fare policy for a route
SELECT get_active_fare_policy(1, 'ADULT', 'STANDARD', CURRENT_TIMESTAMP);

-- View fare policy history
SELECT * FROM fare_policy_history WHERE fare_policy_id = 1;
```

### Reservation Status History

**Purpose**: Track all status changes for reservations.

**Features**:
- Records old status, new status, timestamp, and reason
- Automatic tracking via triggers
- Useful for audit and analytics

---

## Functions and Procedures

### Helper Functions

#### `get_current_vat_rate()`
Returns the current VAT rate.

**Returns**: `DECIMAL(5, 2)`

**Usage**:
```sql
SELECT get_current_vat_rate();
```

#### `get_vat_rate_for_date(check_date TIMESTAMP)`
Returns the VAT rate effective for a specific date.

**Returns**: `DECIMAL(5, 2)`

**Usage**:
```sql
SELECT get_vat_rate_for_date('2024-01-15 10:00:00'::TIMESTAMP);
```

#### `get_active_fare_policy(route_id, category, class, date)`
Returns the active fare policy for a route, category, and class at a specific date.

**Returns**: `DECIMAL(10, 2)`

**Usage**:
```sql
SELECT get_active_fare_policy(1, 'ADULT', 'STANDARD', CURRENT_TIMESTAMP);
```

### Fare Calculation Functions

#### `calculate_fare_with_vat(base_price, seat_count, vat_rate)`
Calculates fare with VAT.

**Returns**: Table with `base_fare`, `vat_amount`, `total_fare`

**Usage**:
```sql
SELECT * FROM calculate_fare_with_vat(25.00, 2, 19.00);
```

#### `calculate_reservation_fare(route_id, category, class, seat_count, date)`
Calculates fare for a reservation.

**Returns**: Table with `base_fare`, `vat_amount`, `total_fare`, `vat_rate`

**Usage**:
```sql
SELECT * FROM calculate_reservation_fare(1, 'ADULT', 'STANDARD', 2, CURRENT_TIMESTAMP);
```

### Availability Check Functions

#### `check_seat_availability(route_id, required_seats)`
Checks if required seats are available.

**Returns**: `BOOLEAN`

**Usage**:
```sql
SELECT check_seat_availability(1, 5);
```

#### `get_available_seats(route_id)`
Returns the number of available seats.

**Returns**: `INTEGER`

**Usage**:
```sql
SELECT get_available_seats(1);
```

### Revenue Calculation Functions

#### `calculate_total_revenue()`
Calculates total revenue from confirmed reservations.

**Returns**: `DECIMAL(15, 2)`

**Usage**:
```sql
SELECT calculate_total_revenue();
```

#### `calculate_route_revenue(route_id)`
Calculates revenue for a specific route.

**Returns**: `DECIMAL(15, 2)`

**Usage**:
```sql
SELECT calculate_route_revenue(1);
```

#### `calculate_revenue_for_period(start_date, end_date)`
Calculates revenue for a date range.

**Returns**: Table with `total_revenue`, `total_vat`, `net_revenue`, `reservation_count`

**Usage**:
```sql
SELECT * FROM calculate_revenue_for_period(
    '2024-01-01'::TIMESTAMP,
    '2024-01-31'::TIMESTAMP
);
```

### Statistics Functions

#### `calculate_route_statistics(route_id)`
Calculates statistics for a route.

**Returns**: Table with statistics

**Usage**:
```sql
SELECT * FROM calculate_route_statistics(1);
```

### Procedures

#### `update_route_availability(route_id)`
Updates the route availability table for a specific route.

**Usage**:
```sql
CALL update_route_availability(1);
```

#### `update_route_statistics(route_id)`
Updates the route statistics table for a specific route.

**Usage**:
```sql
CALL update_route_statistics(1);
```

#### `cancel_reservation(reservation_id, cancelled_by, reason)`
Cancels a reservation and updates related tables.

**Usage**:
```sql
CALL cancel_reservation(1, 'ADMIN', 'Customer request');
```

#### `confirm_reservation(reservation_id, confirmed_by)`
Confirms a reservation and updates related tables.

**Usage**:
```sql
CALL confirm_reservation(1, 'ADMIN');
```

#### `generate_revenue_summary(summary_date)`
Generates revenue summary for a specific date.

**Usage**:
```sql
CALL generate_revenue_summary('2024-01-15'::DATE);
```

#### `initialize_all_route_availability()`
Initializes route availability for all routes.

**Usage**:
```sql
CALL initialize_all_route_availability();
```

#### `initialize_all_route_statistics()`
Initializes route statistics for all routes.

**Usage**:
```sql
CALL initialize_all_route_statistics();
```

---

## Triggers

### Validation Triggers

#### 1. `trigger_validate_route_creation`
**Purpose**: Validates route creation.

**Validations**:
- Prevents circular routes (origin = destination)
- (Optional) Prevents duplicate routes

**Table**: `routes`
**Timing**: BEFORE INSERT OR UPDATE

#### 2. `trigger_validate_reservation_times`
**Purpose**: Validates reservation times.

**Validations**:
- Arrival time must be after departure time
- Departure time cannot be more than 1 day in the past
- Departure time cannot be more than 1 year in the future

**Table**: `reservations`
**Timing**: BEFORE INSERT OR UPDATE

#### 3. `trigger_validate_seat_capacity`
**Purpose**: Validates seat capacity.

**Validations**:
- Checks if enough seats are available
- Prevents overbooking
- Validates seat count (1-10)

**Table**: `reservations`
**Timing**: BEFORE INSERT OR UPDATE

#### 4. `trigger_validate_fare_policy_dates`
**Purpose**: Validates fare policy dates.

**Validations**:
- Effective end date must be after start date
- Prevents overlapping active policies

**Table**: `fare_policies`
**Timing**: BEFORE INSERT OR UPDATE

#### 5. `trigger_validate_vat_rate_dates`
**Purpose**: Validates VAT rate dates.

**Validations**:
- Effective end date must be after start date
- Prevents overlapping VAT rates

**Table**: `vat_rates`
**Timing**: BEFORE INSERT OR UPDATE

#### 6. `trigger_validate_station_status`
**Purpose**: Validates station status changes.

**Validations**:
- Cannot close station if it has active routes

**Table**: `stations`
**Timing**: BEFORE UPDATE

### Denormalization Triggers

#### 1. `trigger_update_reservation_denormalized`
**Purpose**: Updates denormalized fields in reservations.

**Actions**:
- Calculates and caches station names
- Calculates and caches fare information (base, VAT, total)
- Updates `updated_at` timestamp

**Table**: `reservations`
**Timing**: BEFORE INSERT OR UPDATE

#### 2. `trigger_update_route_availability`
**Purpose**: Updates route availability after reservation changes.

**Actions**:
- Updates `route_availability` table
- Updates `route_statistics` table

**Table**: `reservations`
**Timing**: AFTER INSERT OR UPDATE OR DELETE

#### 3. `trigger_maintain_reservation_status_history`
**Purpose**: Maintains reservation status history.

**Actions**:
- Records status changes in `reservation_status_history`

**Table**: `reservations`
**Timing**: AFTER UPDATE

#### 4. `trigger_maintain_fare_policy_history`
**Purpose**: Maintains fare policy history.

**Actions**:
- Records fare policy changes in `fare_policy_history`

**Table**: `fare_policies`
**Timing**: AFTER INSERT OR UPDATE OR DELETE

#### 5. `trigger_update_route_on_change`
**Purpose**: Updates route on change.

**Actions**:
- Updates `updated_at` timestamp
- Updates availability if capacity changed

**Table**: `routes`
**Timing**: BEFORE UPDATE

#### 6. `trigger_update_station_on_change`
**Purpose**: Updates station on change.

**Actions**:
- Updates `updated_at` timestamp
- Updates denormalized station names in reservations if name changed

**Table**: `stations`
**Timing**: BEFORE UPDATE

### Special Case Triggers

#### 1. `trigger_prevent_station_deletion`
**Purpose**: Prevents deletion of stations with routes.

**Table**: `stations`
**Timing**: BEFORE DELETE

#### 2. `trigger_prevent_route_deletion`
**Purpose**: Prevents deletion of routes with reservations.

**Table**: `routes`
**Timing**: BEFORE DELETE

#### 3. `trigger_create_route_availability`
**Purpose**: Automatically creates route availability when route is created.

**Table**: `routes`
**Timing**: AFTER INSERT

#### 4. `trigger_create_route_statistics`
**Purpose**: Automatically creates route statistics when route is created.

**Table**: `routes`
**Timing**: AFTER INSERT

---

## Special Cases and Exceptions

### 1. Circular Routes

**Problem**: A route cannot have the same origin and destination.

**Solution**: Check constraint and trigger validation.

```sql
CONSTRAINT chk_route_different_stations CHECK (origin_station_id != destination_station_id)
```

**Trigger**: `trigger_validate_route_creation`

### 2. Overbooking Prevention

**Problem**: Prevent booking more seats than available.

**Solution**: Capacity validation trigger.

**Trigger**: `trigger_validate_seat_capacity`

**Logic**:
- Calculates currently booked seats
- Checks if adding new reservation would exceed capacity
- Raises exception if capacity exceeded

### 3. Invalid Time Sequences

**Problem**: Arrival time must be after departure time.

**Solution**: Check constraint and trigger validation.

```sql
CONSTRAINT chk_reservation_times CHECK (arrival_time > departure_time)
```

**Trigger**: `trigger_validate_reservation_times`

### 4. Past Departure Times

**Problem**: Cannot book reservations for past departures (except 1 day grace period).

**Solution**: Check constraint and trigger validation.

```sql
CONSTRAINT chk_reservation_future_departure CHECK (departure_time > CURRENT_TIMESTAMP - INTERVAL '1 day')
```

### 5. Overlapping Fare Policies

**Problem**: Only one active fare policy per route/category/class at a time.

**Solution**: Unique constraint and trigger validation.

**Constraint**: `uk_fare_policy_active` with NULL handling

**Trigger**: `trigger_validate_fare_policy_dates`

### 6. Overlapping VAT Rates

**Problem**: Only one active VAT rate at a time.

**Solution**: Trigger validation.

**Trigger**: `trigger_validate_vat_rate_dates`

### 7. Station Closure with Active Routes

**Problem**: Cannot close a station if it has active routes.

**Solution**: Trigger validation.

**Trigger**: `trigger_validate_station_status`

### 8. Deletion Prevention

**Problem**: Cannot delete stations/routes if they are in use.

**Solution**: Triggers prevent deletion.

**Triggers**: 
- `trigger_prevent_station_deletion`
- `trigger_prevent_route_deletion`

### 9. Missing Fare Policy

**Problem**: Reservation requires a fare policy, but none exists.

**Solution**: Function raises exception with helpful message.

**Function**: `get_active_fare_policy()`

### 10. Email Format Validation

**Problem**: Ensure email addresses are in valid format.

**Solution**: Check constraint with regex.

```sql
CONSTRAINT chk_reservation_email_format CHECK (passenger_email IS NULL OR passenger_email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
```

### 11. Seat Count Limits

**Problem**: Limit number of seats per reservation.

**Solution**: Check constraint.

```sql
CONSTRAINT chk_reservation_seat_count CHECK (seat_count > 0 AND seat_count <= 10)
```

### 12. Status Transitions

**Problem**: Ensure valid status transitions.

**Solution**: Check constraint and procedure validation.

**Statuses**: PENDING, CONFIRMED, CANCELLED, COMPLETED, NO_SHOW

**Procedures**: 
- `confirm_reservation()`: Validates transition to CONFIRMED
- `cancel_reservation()`: Validates transition to CANCELLED

---

## Installation and Setup

### Prerequisites

- PostgreSQL 15 or higher
- Database user with CREATE privileges
- psql command-line tool (optional)

### Step-by-Step Installation

#### Step 1: Create Database

```sql
CREATE DATABASE transportation_db;
\c transportation_db
```

#### Step 2: Run Initialization Script

**Option A: Using psql**

```bash
cd database
psql -U username -d transportation_db -f 00_init_database.sql
```

**Option B: Running Scripts Individually**

```bash
psql -U username -d transportation_db -f 01_schema_core.sql
psql -U username -d transportation_db -f 02_functions_procedures.sql
psql -U username -d transportation_db -f 03_triggers.sql
psql -U username -d transportation_db -f 04_sample_data.sql  # Optional
```

#### Step 3: Initialize Denormalized Tables

```sql
CALL initialize_all_route_availability();
CALL initialize_all_route_statistics();
```

#### Step 4: Verify Installation

```sql
-- Check tables
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public' 
ORDER BY table_name;

-- Check functions
SELECT routine_name FROM information_schema.routines 
WHERE routine_schema = 'public' 
ORDER BY routine_name;

-- Check triggers
SELECT trigger_name, event_object_table 
FROM information_schema.triggers 
WHERE trigger_schema = 'public' 
ORDER BY trigger_name;
```

### Docker Setup

If using Docker, add the initialization to your `docker-compose.yml`:

```yaml
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: transportation_db
      POSTGRES_USER: user
      POSTGRES_PASSWORD: password
    volumes:
      - ./database:/docker-entrypoint-initdb.d
    ports:
      - "5438:5432"
```

PostgreSQL will automatically run SQL scripts in `/docker-entrypoint-initdb.d` on first startup.

---

## Usage Examples

### Creating a Station

```sql
INSERT INTO stations (name, description, status, created_by)
VALUES ('New Station', 'Description', 'ACTIVE', 'ADMIN');
```

### Creating a Route

```sql
INSERT INTO routes (origin_station_id, destination_station_id, vehicle_capacity, created_by)
VALUES (1, 2, 50, 'ADMIN');
```

### Creating a Fare Policy

```sql
INSERT INTO fare_policies (route_id, passenger_category, vehicle_class, base_price, effective_from, created_by)
VALUES (1, 'ADULT', 'STANDARD', 25.00, CURRENT_TIMESTAMP, 'ADMIN');
```

### Creating a Reservation

```sql
INSERT INTO reservations (
    route_id,
    passenger_name,
    passenger_email,
    seat_count,
    status,
    passenger_category,
    vehicle_class,
    departure_time,
    arrival_time,
    created_by
)
VALUES (
    1,
    'John Doe',
    'john.doe@example.com',
    2,
    'PENDING',
    'ADULT',
    'STANDARD',
    CURRENT_TIMESTAMP + INTERVAL '2 days',
    CURRENT_TIMESTAMP + INTERVAL '2 days' + INTERVAL '45 minutes',
    'ADMIN'
);
```

### Checking Seat Availability

```sql
SELECT get_available_seats(1);
```

### Calculating Fare

```sql
SELECT * FROM calculate_reservation_fare(1, 'ADULT', 'STANDARD', 2, CURRENT_TIMESTAMP);
```

### Cancelling a Reservation

```sql
CALL cancel_reservation(1, 'ADMIN', 'Customer request');
```

### Updating VAT Rate

```sql
-- End current VAT rate
UPDATE vat_rates 
SET effective_to = CURRENT_TIMESTAMP 
WHERE effective_to IS NULL;

-- Insert new VAT rate
INSERT INTO vat_rates (rate_percentage, effective_from, description, created_by)
VALUES (20.00, CURRENT_TIMESTAMP, 'New VAT rate', 'ADMIN');
```

### Updating Fare Policy

```sql
-- End current policy
UPDATE fare_policies 
SET effective_to = CURRENT_TIMESTAMP, status = 'INACTIVE'
WHERE route_id = 1 
  AND passenger_category = 'ADULT' 
  AND vehicle_class = 'STANDARD'
  AND effective_to IS NULL;

-- Insert new policy
INSERT INTO fare_policies (route_id, passenger_category, vehicle_class, base_price, effective_from, created_by)
VALUES (1, 'ADULT', 'STANDARD', 30.00, CURRENT_TIMESTAMP, 'ADMIN');
```

### Generating Revenue Report

```sql
-- Generate summary for a date
CALL generate_revenue_summary('2024-01-15'::DATE);

-- Query summary
SELECT * FROM revenue_summary 
WHERE summary_date = '2024-01-15'::DATE;
```

### Viewing Route Statistics

```sql
SELECT * FROM route_statistics WHERE route_id = 1;
```

### Viewing Reservation History

```sql
SELECT * FROM reservation_status_history 
WHERE reservation_id = 1 
ORDER BY changed_at;
```

---

## Maintenance and Best Practices

### Regular Maintenance Tasks

#### 1. Update Route Availability

```sql
-- Update all routes
CALL initialize_all_route_availability();

-- Update specific route
CALL update_route_availability(1);
```

#### 2. Update Route Statistics

```sql
-- Update all routes
CALL initialize_all_route_statistics();

-- Update specific route
CALL update_route_statistics(1);
```

#### 3. Generate Revenue Summaries

```sql
-- Generate for yesterday
CALL generate_revenue_summary(CURRENT_DATE - INTERVAL '1 day');

-- Generate for a date range (use in a loop)
DO $$
DECLARE
    date_val DATE;
BEGIN
    FOR date_val IN SELECT generate_series('2024-01-01'::DATE, '2024-01-31'::DATE, '1 day'::INTERVAL)::DATE
    LOOP
        CALL generate_revenue_summary(date_val);
    END LOOP;
END $$;
```

#### 4. Archive Old Data

```sql
-- Archive completed reservations older than 1 year
CREATE TABLE reservations_archive AS
SELECT * FROM reservations
WHERE status = 'COMPLETED'
  AND created_at < CURRENT_TIMESTAMP - INTERVAL '1 year';

DELETE FROM reservations
WHERE status = 'COMPLETED'
  AND created_at < CURRENT_TIMESTAMP - INTERVAL '1 year';
```

### Best Practices

#### 1. Always Use Procedures for Status Changes

✅ **Good**:
```sql
CALL confirm_reservation(1, 'ADMIN');
CALL cancel_reservation(1, 'ADMIN', 'Reason');
```

❌ **Bad**:
```sql
UPDATE reservations SET status = 'CONFIRMED' WHERE id = 1;
```

#### 2. Use Functions for Calculations

✅ **Good**:
```sql
SELECT * FROM calculate_reservation_fare(1, 'ADULT', 'STANDARD', 2, CURRENT_TIMESTAMP);
```

❌ **Bad**:
```sql
-- Manual calculation in application
```

#### 3. Check Availability Before Creating Reservation

```sql
IF check_seat_availability(1, 5) THEN
    -- Create reservation
ELSE
    -- Handle error
END IF;
```

#### 4. Use Temporal Functions for Historical Queries

```sql
-- Get VAT rate for a specific date
SELECT get_vat_rate_for_date('2024-01-15'::TIMESTAMP);

-- Get fare policy for a specific date
SELECT get_active_fare_policy(1, 'ADULT', 'STANDARD', '2024-01-15'::TIMESTAMP);
```

#### 5. Monitor Denormalized Tables

```sql
-- Check if denormalized tables are in sync
SELECT 
    r.id,
    r.vehicle_capacity AS route_capacity,
    ra.total_capacity AS availability_capacity,
    r.vehicle_capacity - ra.total_capacity AS diff
FROM routes r
JOIN route_availability ra ON r.id = ra.route_id
WHERE r.vehicle_capacity != ra.total_capacity;
```

#### 6. Regular Index Maintenance

```sql
-- Analyze tables for query optimization
ANALYZE reservations;
ANALYZE routes;
ANALYZE fare_policies;

-- Reindex if needed
REINDEX TABLE reservations;
```

#### 7. Backup Strategy

- Regular backups of the database
- Backup before major schema changes
- Test restore procedures

#### 8. Monitoring and Logging

- Monitor trigger execution times
- Log slow queries
- Monitor denormalized table sync

### Performance Optimization

#### 1. Index Usage

All frequently queried columns are indexed:
- Foreign keys
- Status columns
- Date/time columns
- Lookup columns (route_id, passenger_category, etc.)

#### 2. Query Optimization

- Use denormalized tables for aggregations
- Use indexes for filtering
- Avoid unnecessary JOINs

#### 3. Denormalized Table Maintenance

- Triggers automatically update denormalized tables
- Manual refresh available via procedures
- Consider scheduled jobs for large updates

### Troubleshooting

#### Issue: Reservation creation fails with capacity error

**Solution**: Check available seats and route capacity

```sql
SELECT * FROM route_availability WHERE route_id = 1;
SELECT * FROM routes WHERE id = 1;
```

#### Issue: Fare calculation returns error

**Solution**: Check if fare policy exists

```sql
SELECT * FROM fare_policies 
WHERE route_id = 1 
  AND passenger_category = 'ADULT' 
  AND vehicle_class = 'STANDARD'
  AND status = 'ACTIVE';
```

#### Issue: Denormalized fields are NULL

**Solution**: Trigger may have failed. Manually update:

```sql
-- Recalculate fare for a reservation
UPDATE reservations 
SET route_id = route_id  -- Trigger will recalculate
WHERE id = 1;
```

#### Issue: Route availability is incorrect

**Solution**: Manually refresh:

```sql
CALL update_route_availability(1);
```

---

## Conclusion

This database schema provides a robust, scalable, and performant foundation for the Transportation Management System. It includes:

- ✅ Comprehensive validation and data integrity
- ✅ Denormalization for performance
- ✅ Temporality for historical tracking
- ✅ Audit trails for compliance
- ✅ Functions and procedures for business logic
- ✅ Triggers for automation
- ✅ Special case handling

For questions or issues, refer to this documentation or contact the development team.

