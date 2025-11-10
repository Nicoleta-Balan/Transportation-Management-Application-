# Database Quick Start Guide

## Quick Installation

```bash
# 1. Create database
createdb transportation_db

# 2. Run initialization script
psql -U username -d transportation_db -f 00_init_database.sql

# 3. (Optional) Load sample data
psql -U username -d transportation_db -f 04_sample_data.sql
```

## Key Features

### 1. Denormalization
- **Route Availability**: Fast seat availability checks
- **Route Statistics**: Pre-calculated route metrics
- **Revenue Summary**: Daily revenue aggregates
- **Cached Fields**: Station names and fare info in reservations

### 2. Temporality
- **VAT Rates**: Track VAT changes over time
- **Fare Policies**: Historical fare tracking
- **Audit Trails**: Complete change history

### 3. Validation
- Capacity checks (prevents overbooking)
- Time validation (arrival > departure)
- Circular route prevention
- Email format validation
- Status transition validation

### 4. Timetables
- Route timetables and entries for recurring schedules
- REST endpoints to manage timetables programmatically

## Common Operations

### Create Reservation
```sql
INSERT INTO reservations (route_id, passenger_name, seat_count, status, 
                         passenger_category, vehicle_class, departure_time, arrival_time)
VALUES (1, 'John Doe', 2, 'PENDING', 'ADULT', 'STANDARD', 
        CURRENT_TIMESTAMP + INTERVAL '2 days', 
        CURRENT_TIMESTAMP + INTERVAL '2 days' + INTERVAL '45 minutes');
```

### Check Availability
```sql
SELECT get_available_seats(1);
SELECT check_seat_availability(1, 5);
```

### Calculate Fare
```sql
SELECT * FROM calculate_reservation_fare(1, 'ADULT', 'STANDARD', 2, CURRENT_TIMESTAMP);
```

### Cancel Reservation
```sql
CALL cancel_reservation(1, 'ADMIN', 'Customer request');
```

### View Timetable
```sql
SELECT tt.id,
       tt.name,
       e.service_day,
       e.departure_time,
       e.arrival_time
FROM route_timetables tt
JOIN route_timetable_entries e ON e.timetable_id = tt.id
WHERE tt.route_id = 1
ORDER BY e.service_day, e.departure_time;
```
### Update VAT Rate
```sql
-- End current rate
UPDATE vat_rates SET effective_to = CURRENT_TIMESTAMP WHERE effective_to IS NULL;

-- Add new rate
INSERT INTO vat_rates (rate_percentage, effective_from, description)
VALUES (20.00, CURRENT_TIMESTAMP, 'New VAT rate');
```

### Generate Revenue Report
```sql
CALL generate_revenue_summary(CURRENT_DATE);
SELECT * FROM revenue_summary WHERE summary_date = CURRENT_DATE;
```

## Important Notes

1. **Always use procedures** for status changes (`confirm_reservation`, `cancel_reservation`)
2. **Triggers automatically update** denormalized tables
3. **Fare calculation** uses VAT and fare policy effective at reservation time
4. **Capacity validation** happens automatically via triggers
5. **Historical data** is preserved (VAT rates, fare policies, status changes)

## Troubleshooting

### Reservation creation fails
- Check available seats: `SELECT * FROM route_availability WHERE route_id = 1;`
- Verify fare policy exists: `SELECT * FROM fare_policies WHERE route_id = 1 AND status = 'ACTIVE';`

### Denormalized fields are NULL
- Triggers should update automatically
- Manually trigger update: `UPDATE reservations SET route_id = route_id WHERE id = 1;`

### Route availability incorrect
- Refresh: `CALL update_route_availability(1);`

## Documentation

For detailed documentation, see [README.md](README.md)

