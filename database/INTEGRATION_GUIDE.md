# Database Integration Guide

## Overview

This guide explains how the database schema is integrated with the Spring Boot application and Docker Compose setup.

## Integration Components

### 1. Docker Compose Configuration

The `docker-compose.yml` file has been updated to:

- **Mount database initialization scripts**: SQL scripts are automatically executed when the PostgreSQL container starts for the first time
- **Configure health checks**: The database container includes a health check to ensure it's ready before the Spring Boot application starts
- **Set environment variables**: Database connection credentials are configured via environment variables
- **Fix password consistency**: Database password is now consistent across all services
- **Provision route timetables**: Timetable tables and sample schedules are created for core routes

### 2. Spring Boot Configuration

The `application.properties` file has been created with:

- **Database connection settings**: Configurable via environment variables
- **JPA/Hibernate configuration**: Set to `validate` mode (schema is managed by SQL scripts)
- **Connection pool settings**: Optimized for production use
- **Logging configuration**: SQL queries can be enabled for debugging

### 3. JPA Entity Updates

The following entities have been updated to match the database schema:

#### Reservation Entity
- Added `passengerEmail` and `passengerPhone` fields
- Added denormalized fields: `originStationName`, `destinationStationName`, `baseFare`, `vatAmount`, `totalFare`
- Updated `TripTimeDetails` mapping to use correct column names
- Denormalized fields are marked as `insertable = false, updatable = false` (populated by database triggers)

#### Route Entity
- Updated `vehicleCapacity` to map to `vehicle_capacity` column

#### FarePolicy Entity
- Updated `price` to map to `base_price` column

#### Timetable Entities
- Added `RouteTimetable` and `RouteTimetableEntry` entities with cascading relationships
- Added repositories, services, and controllers to manage route schedules

## Database Initialization Flow

When the Docker container starts:

1. **PostgreSQL container starts** and creates the database
2. **SQL scripts are executed in order**:
   - `01_schema_core.sql`: Creates all tables, indexes, and constraints
   - `02_functions_procedures.sql`: Creates all functions and stored procedures
   - `03_triggers.sql`: Creates all triggers for validation and denormalization
   - `04_init_denormalized.sql`: Initializes denormalized tables (if routes exist)
   - `05_sample_data.sql`: Loads sample stations, routes, timetables, fares, and reservations
3. **Database health check** verifies the database is ready
4. **Spring Boot application starts** and validates the schema matches the JPA entities

## Environment Variables

The following environment variables can be configured:

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | `localhost` | Database host (use `db` in Docker) |
| `DB_PORT` | `5438` | Database port (use `5432` in Docker) |
| `DB_NAME` | `tms_db` | Database name |
| `DB_USER` | `tms_user` | Database user |
| `DB_PASSWORD` | `gi` | Database password |

## Running the Application

### 1. Build and Start with Docker Compose

```bash
# Build and start all services
docker compose up --build -d

# View logs
docker compose logs -f app
docker compose logs -f db

# Stop services
docker compose down

# Stop and remove volumes (clean slate)
docker compose down -v
```

### 2. Verify Database Initialization

```bash
# Connect to the database
docker exec -it tms_postgres_db psql -U tms_user -d tms_db

# Check tables
\dt

# Check functions
\df

# Check triggers
SELECT trigger_name, event_object_table 
FROM information_schema.triggers 
WHERE trigger_schema = 'public';

# Exit
\q
```

### 3. Load Sample Data (Optional)

```bash
# Connect to the database
docker exec -it tms_postgres_db psql -U tms_user -d tms_db

# Run sample data script
\i /docker-entrypoint-initdb.d/05_sample_data.sql

# Or copy the file into the container first
docker cp database/04_sample_data.sql tms_postgres_db:/tmp/
docker exec -it tms_postgres_db psql -U tms_user -d tms_db -f /tmp/04_sample_data.sql
```

### 4. Exercise timetable endpoints

```bash
# List timetables for a route
curl http://localhost:8085/api/routes/1/timetables

# Add a new timetable with two entries
curl -X POST http://localhost:8085/api/routes/1/timetables \
  -H "Content-Type: application/json" \
  -d '{
        "name": "Late Afternoon Runs",
        "description": "Extra services before evening peak",
        "effectiveFrom": "2025-01-01",
        "entries": [
          {"serviceDay": "MONDAY", "departureTime": "16:30", "arrivalTime": "17:10"},
          {"serviceDay": "FRIDAY", "departureTime": "17:00", "arrivalTime": "17:45"}
        ]
      }'
```

## Troubleshooting

### Database Connection Issues

**Problem**: Spring Boot application cannot connect to the database

**Solution**:
1. Check if the database container is running: `docker compose ps`
2. Check database logs: `docker compose logs db`
3. Verify environment variables in `docker-compose.yml`
4. Check if the database is healthy: `docker compose ps db` (should show "healthy")

### Schema Validation Errors

**Problem**: JPA schema validation fails

**Solution**:
1. Verify that all SQL scripts ran successfully: Check database logs
2. Check if entities match the database schema: Compare JPA entity annotations with database columns
3. Temporarily set `spring.jpa.hibernate.ddl-auto=update` to see what's different
4. Check application logs for specific validation errors

### Triggers Not Working

**Problem**: Denormalized fields are not populated

**Solution**:
1. Verify triggers are created: `SELECT * FROM information_schema.triggers WHERE trigger_schema = 'public';`
2. Check trigger functions: `\df update_reservation_denormalized_fields`
3. Test trigger manually: Insert a reservation and check if fields are populated
4. Check database logs for trigger errors

### Database Initialization Fails

**Problem**: SQL scripts fail to execute

**Solution**:
1. Check database logs: `docker compose logs db`
2. Verify SQL script syntax: Run scripts manually in psql
3. Check file permissions: Scripts should be readable
4. Remove volume and restart: `docker compose down -v && docker compose up --build -d`

## Development Workflow

### 1. Making Schema Changes

1. Update SQL scripts in `database/` directory
2. Update JPA entities to match schema changes
3. Rebuild and restart containers: `docker compose down -v && docker compose up --build -d`
4. Test changes

### 2. Adding New Entities

1. Create entity class with JPA annotations
2. Update database schema if needed
3. Create repository interface
4. Update service layer
5. Test integration

### 3. Debugging Database Issues

1. Enable SQL logging in `application.properties`:
   ```properties
   spring.jpa.show-sql=true
   logging.level.org.hibernate.SQL=DEBUG
   logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
   ```

2. Connect to database and run queries:
   ```bash
   docker exec -it tms_postgres_db psql -U tms_user -d tms_db
   ```

3. Check trigger execution:
   ```sql
   -- Enable trigger logging
   SET log_statement = 'all';
   ```

## Best Practices

1. **Always use transactions**: Wrap database operations in `@Transactional`
2. **Use repository methods**: Don't write raw SQL unless necessary
3. **Validate input**: Use JPA validation annotations
4. **Handle exceptions**: Catch database exceptions and return appropriate HTTP responses
5. **Test database integration**: Write integration tests for database operations
6. **Monitor performance**: Use connection pooling and optimize queries
7. **Backup regularly**: Set up database backups for production

## Production Considerations

1. **Change default password**: Update `POSTGRES_PASSWORD` in `docker-compose.yml`
2. **Use secrets management**: Don't hardcode passwords in configuration files
3. **Enable SSL**: Configure PostgreSQL to use SSL connections
4. **Set up monitoring**: Monitor database performance and health
5. **Configure backups**: Set up automated database backups
6. **Optimize indexes**: Review and optimize database indexes
7. **Set connection limits**: Configure appropriate connection pool sizes
8. **Enable query logging**: Log slow queries for optimization

## Additional Resources

- [Database README](README.md): Comprehensive database documentation
- [Quick Start Guide](QUICK_START.md): Quick reference for common operations
- [PostgreSQL Documentation](https://www.postgresql.org/docs/): Official PostgreSQL documentation
- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa): Spring Data JPA documentation

