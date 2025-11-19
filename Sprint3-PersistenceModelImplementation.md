# Persistence Model Implementation

## Architecture Overview

The Transportation Management System uses a **pure JPA/Hibernate persistence model** that manages the entire database schema and data access layer. The system follows a layered architecture where JPA entities map to database tables, Spring Data JPA repositories provide data access, and business services orchestrate operations.

```
HTTP Request → Controller → Service → Repository (Spring Data JPA) → JPA/Hibernate → PostgreSQL Database
```

---

## 1. Database Schema Management

### 1.1 JPA/Hibernate Schema Generation

**Current Approach**: Pure JPA/Hibernate schema management

**Configuration** (`application.properties`):
```properties
spring.jpa.hibernate.ddl-auto=update
spring.flyway.enabled=false
```

**How it works**:
- JPA/Hibernate automatically creates and updates database tables based on `@Entity` annotations
- On application startup, Hibernate compares entity definitions with database schema
- Missing tables are created, missing columns are added
- Schema changes are applied automatically (no manual SQL migrations needed)

**Benefits**:
- ✅ Single source of truth (entity classes define schema)
- ✅ Type-safe schema definition
- ✅ Automatic schema evolution
- ✅ No migration file management

**Entities managed by JPA** (15 total):
- `AdminUser`, `FarePolicy`, `FarePolicyHistory`, `RegularUser`, `Reservation`, `ReservationStatusHistory`, `RevenueSummary`, `Route`, `RouteAvailability`, `RouteStatistics`, `RouteTimetable`, `RouteTimetableEntry`, `Station`, `User`, `VatRate`

---

## 2. Entity Model Layer

### 2.1 Base Entity Pattern

**BaseEntity** - Common timestamp fields with automatic management

**Location**: `src/main/java/multitier/trans/model/BaseEntity.java`

```java
@MappedSuperclass
@EntityListeners(BaseEntityListener.class)
public abstract class BaseEntity {
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

**Entities extending BaseEntity**:
- `RouteTimetable` - `src/main/java/multitier/trans/model/RouteTimetable.java`
- `User` (and subclasses `RegularUser`, `AdminUser`) - `src/main/java/multitier/trans/model/User.java`
- `RouteTimetableEntry` - `src/main/java/multitier/trans/model/RouteTimetableEntry.java`

**How it works**:
- `@MappedSuperclass` - Fields are inherited by subclasses but BaseEntity is not a table itself
- `@EntityListeners(BaseEntityListener.class)` - Automatically applies listener to all subclasses
- Timestamps are automatically managed via `@PrePersist` and `@PreUpdate` callbacks

### 2.2 Entity Listeners

#### BaseEntityListener

**Location**: `src/main/java/multitier/trans/model/BaseEntityListener.java`

**Callbacks implemented**:
- `@PrePersist` - Sets `createdAt` and `updatedAt` before entity is saved
- `@PreUpdate` - Updates `updatedAt` before entity is updated
- `@PreRemove` - Hook for audit logging or cleanup before deletion

**Applied to**: All entities extending `BaseEntity`

**Usage example**:
```java
RouteTimetable timetable = new RouteTimetable();
timetable.setName("Weekday Schedule");
timetableRepository.save(timetable);
// → BaseEntityListener.prePersist() automatically sets createdAt and updatedAt
```

#### FarePolicyHistoryListener

**Location**: `src/main/java/multitier/trans/model/FarePolicyHistoryListener.java`

**Callbacks implemented**:
- `@PostPersist` - Records fare policy creation in history table
- `@PostUpdate` - Records fare policy updates in history table
- `@PreRemove` - Records fare policy deletion in history table

**Applied to**: `FarePolicy` entity via `@EntityListeners(FarePolicyHistoryListener.class)`

**How it accesses Spring beans**:
- Uses `ApplicationContextProvider` to get `FarePolicyHistoryRepository`
- Entity Listeners cannot use `@Autowired` directly (not Spring-managed)

**Usage example**:
```java
FarePolicy policy = new FarePolicy();
policy.setPrice(new BigDecimal("50.00"));
farePolicyRepository.save(policy);
// → FarePolicyHistoryListener.onFarePolicyCreated() automatically creates history record
```

#### ReservationStatusHistoryListener

**Location**: `src/main/java/multitier/trans/model/ReservationStatusHistoryListener.java`

**Callbacks implemented**:
- `@PostUpdate` - Placeholder for reservation status change tracking

**Note**: Status changes are primarily tracked in `ReservationServiceImpl.recordStatusHistory()` method, as old status values are needed and not available in `@PostUpdate`.

---

## 3. Entity Relationships

### 3.1 Inheritance: User Hierarchy

**Strategy**: `SINGLE_TABLE` inheritance

**Base Entity**: `User`
- Location: `src/main/java/multitier/trans/model/User.java`
- Annotation: `@Inheritance(strategy = InheritanceType.SINGLE_TABLE)`
- Discriminator: `@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING, length = 20)`
- Default Value: `@DiscriminatorValue("USER")`

**Subclasses**:
- `RegularUser` - `@DiscriminatorValue("USER")` - `src/main/java/multitier/trans/model/RegularUser.java`
- `AdminUser` - `@DiscriminatorValue("ADMIN")` - `src/main/java/multitier/trans/model/AdminUser.java`

**Database Structure**:
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    user_type VARCHAR(20) NOT NULL,  -- Discriminator column
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    -- ... other fields
);
```

**How it works**:
- All user types stored in single `users` table
- `user_type` column distinguishes between subclasses
- JPA automatically instantiates correct subclass based on discriminator value
- No joins required - efficient queries

**Usage**:
```java
// Creating a regular user
RegularUser user = new RegularUser("john", "john@example.com", "hashedPassword");
userRepository.save(user);
// → INSERT INTO users (user_type, username, email, ...) VALUES ('USER', 'john', ...)

// Querying returns correct subclass
User found = userRepository.findByUsername("john");
// → Returns RegularUser instance (user_type = 'USER')
```

### 3.2 Many-to-One Relationships

**Pattern**: Child entity has foreign key to parent

**Examples**:

1. **Reservation → User**
   - Location: `src/main/java/multitier/trans/model/Reservation.java`
   - Annotation: `@ManyToOne` with `@JoinColumn(name = "user_id", nullable = false)`
   - Usage: Many reservations belong to one user

2. **Reservation → Route**
   - Location: `src/main/java/multitier/trans/model/Reservation.java`
   - Annotation: `@ManyToOne` with `@JoinColumn(name = "route_id", nullable = false)`
   - Usage: Many reservations belong to one route

3. **Route → Station (origin/destination)**
   - Location: `src/main/java/multitier/trans/model/Route.java`
   - Annotations: 
     - `originStation` - `@ManyToOne` with `@JoinColumn(name = "origin_station_id", nullable = false)`
     - `destinationStation` - `@ManyToOne` with `@JoinColumn(name = "destination_station_id", nullable = false)`
   - Usage: Many routes can have same origin/destination stations

4. **FarePolicy → Route**
   - Location: `src/main/java/multitier/trans/model/FarePolicy.java`
   - Annotation: `@ManyToOne` with `@JoinColumn(name = "route_id", nullable = false)`
   - Usage: Many fare policies belong to one route

5. **RouteTimetableEntry → RouteTimetable**
   - Location: `src/main/java/multitier/trans/model/RouteTimetableEntry.java`
   - Annotation: `@ManyToOne(fetch = FetchType.LAZY)` with `@JoinColumn(name = "timetable_id", nullable = false)`
   - Usage: Many entries belong to one timetable

**Database Structure**:
```sql
-- Child table has foreign key
CREATE TABLE reservations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,      -- Foreign key
    route_id BIGINT NOT NULL,     -- Foreign key
    ...
    CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_reservation_route FOREIGN KEY (route_id) REFERENCES routes(id)
);
```

### 3.3 One-to-Many Relationships: Component Entities

**Pattern**: Parent entity manages collection of child entities

**Example**: RouteTimetable → RouteTimetableEntry

**Location**: `src/main/java/multitier/trans/model/RouteTimetable.java`

```java
@OneToMany(
    mappedBy = "timetable",           // Inverse side - references owning side
    cascade = CascadeType.ALL,        // All operations cascade to children
    orphanRemoval = true,             // Remove orphans when removed from collection
    fetch = FetchType.LAZY           // Lazy loading (default for @OneToMany)
)
private List<RouteTimetableEntry> entries = new ArrayList<>();
```

**Key Parameters**:

1. **`mappedBy = "timetable"`**:
   - Indicates this is the **inverse side** (non-owning side)
   - The **owning side** is `RouteTimetableEntry.timetable` (has `@JoinColumn`)
   - Foreign key is managed by the `@ManyToOne` side
   - No `@JoinColumn` needed here

2. **`cascade = CascadeType.ALL`**:
   - Operations on parent cascade to children
   - Save/update/delete timetable automatically saves/updates/deletes entries
   - Prevents manual management of child entities

3. **`orphanRemoval = true`**:
   - Removing entry from collection automatically deletes it from database
   - Ensures no orphaned entries exist without a parent
   - Different from `CascadeType.REMOVE` (which only applies when parent is deleted)

4. **`fetch = FetchType.LAZY`** (default):
   - Related entities loaded only when accessed
   - Performance optimization - avoids loading large collections unnecessarily

**Component Entity**: RouteTimetableEntry
- Location: `src/main/java/multitier/trans/model/RouteTimetableEntry.java`
- Has own identity (`@Id`) and table
- Managed as component via cascade and orphanRemoval
- Can be queried independently if needed

**Database Structure**:
```sql
-- Parent table
CREATE TABLE route_timetables (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    ...
);

-- Child table (has foreign key)
CREATE TABLE route_timetable_entries (
    id BIGSERIAL PRIMARY KEY,
    timetable_id BIGINT NOT NULL,  -- Foreign key
    service_day VARCHAR(10) NOT NULL,
    departure_time TIME NOT NULL,
    ...
    CONSTRAINT fk_entry_timetable FOREIGN KEY (timetable_id) 
        REFERENCES route_timetables(id) ON DELETE CASCADE
);
```

**Usage Example**:
```java
// Create timetable with entries
RouteTimetable timetable = new RouteTimetable();
timetable.setName("Weekday Schedule");

// Add entries (cascade saves them automatically)
RouteTimetableEntry mondayEntry = new RouteTimetableEntry();
mondayEntry.setServiceDay(DayOfWeek.MONDAY);
mondayEntry.setDepartureTime(LocalTime.of(8, 0));
timetable.addEntry(mondayEntry);  // Sets bidirectional relationship

// Save timetable - entries are automatically saved (cascade)
timetableRepository.save(timetable);
// → INSERT INTO route_timetables (...) VALUES (...)
// → INSERT INTO route_timetable_entries (timetable_id, ...) VALUES (1, ...)
```

---

## 4. Value Objects

### 4.1 Embeddable Value Objects

**Pattern**: `@Embeddable` / `@Embedded` for value objects stored in same table

**Example**: TripTimeDetails

**Embeddable Class**:
- Location: `src/main/java/multitier/trans/model/TripTimeDetails.java`
- Annotation: `@Embeddable`
- Fields: `departureTime`, `arrivalTime` (both `LocalDateTime`)

**Entity using @Embedded**:
- Location: `src/main/java/multitier/trans/model/Reservation.java`
- Field: `tripDetails` - `@Embedded` with `@AttributeOverrides`
- Attribute Overrides:
  - `departureTime` → `departure_time` column
  - `arrivalTime` → `arrival_time` column

**Database Structure**:
```sql
CREATE TABLE reservations (
    id BIGSERIAL PRIMARY KEY,
    ...
    departure_time TIMESTAMP NOT NULL,  -- From TripTimeDetails
    arrival_time TIMESTAMP NOT NULL,     -- From TripTimeDetails
    ...
);
```

**Characteristics**:
- ✅ Stored in same table as parent (no separate table)
- ✅ No separate identity (no `@Id`)
- ✅ Cannot be shared between entities
- ✅ Simple value grouping

**Usage**:
```java
Reservation reservation = new Reservation();
TripTimeDetails tripDetails = new TripTimeDetails(
    LocalDateTime.of(2025, 1, 15, 10, 0),  // departure
    LocalDateTime.of(2025, 1, 15, 11, 30)  // arrival
);
reservation.setTripDetails(tripDetails);
// → Fields stored as columns in reservations table
```

### 4.2 Enums with @Enumerated

**Pattern**: Java enums stored as strings in database

**Enums using @Enumerated**:

1. **PassengerCategory**
   - Location: `src/main/java/multitier/trans/model/enums/PassengerCategory.java`
   - Values: `ADULT`, `CHILD`, `SENIOR`, `STUDENT`
   - Used in: `FarePolicy`, `Reservation`, `FarePolicyHistory`
   - Annotation: `@Enumerated(EnumType.STRING)`

2. **VehicleClass**
   - Location: `src/main/java/multitier/trans/model/enums/VehicleClass.java`
   - Values: `STANDARD`, `PREMIUM`, `LUXURY`
   - Used in: `FarePolicy`, `Reservation`, `FarePolicyHistory`
   - Annotation: `@Enumerated(EnumType.STRING)`

3. **DayOfWeek** (Java standard enum)
   - Used in: `RouteTimetableEntry.serviceDay`
   - Annotation: `@Enumerated(EnumType.STRING)`

**Database Storage**:
```sql
CREATE TABLE reservations (
    ...
    passenger_category VARCHAR(20) NOT NULL,  -- Stores "ADULT", "CHILD", etc.
    vehicle_class VARCHAR(20) NOT NULL,       -- Stores "STANDARD", "PREMIUM", etc.
    ...
);
```

**How it works**:
- Enum values stored as readable strings (`"ADULT"`, `"STANDARD"`) instead of numbers
- JPA automatically converts between Java enum and database string
- Type safety: compiler ensures only valid enum values are used
- JSON serialization: Spring automatically converts JSON strings to enum values

### 4.3 Enums with @Convert

**Pattern**: Java enums with custom AttributeConverter for type-safe conversion

**Enums using @Convert**:

1. **ReservationStatus**
   - Location: `src/main/java/multitier/trans/model/enums/ReservationStatus.java`
   - Values: `CONFIRMED`, `CANCELLED`, `PENDING`
   - Converter: `ReservationStatusConverter` - `src/main/java/multitier/trans/model/converter/ReservationStatusConverter.java`
   - Used in: `Reservation.status` field

2. **PolicyStatus**
   - Location: `src/main/java/multitier/trans/model/enums/PolicyStatus.java`
   - Values: `ACTIVE`, `INACTIVE`
   - Converter: `PolicyStatusConverter` - `src/main/java/multitier/trans/model/converter/PolicyStatusConverter.java`
   - Used in: `FarePolicy.status` field

3. **StationStatus**
   - Location: `src/main/java/multitier/trans/model/enums/StationStatus.java`
   - Values: `ACTIVE`, `CLOSED`, `MAINTENANCE`
   - Converter: `StationStatusConverter` - `src/main/java/multitier/trans/model/converter/StationStatusConverter.java`
   - Used in: `Station.status` field

4. **TimetableStatus**
   - Location: `src/main/java/multitier/trans/model/enums/TimetableStatus.java`
   - Values: `ACTIVE`, `INACTIVE`
   - Converter: `TimetableStatusConverter` - `src/main/java/multitier/trans/model/converter/TimetableStatusConverter.java`
   - Used in: `RouteTimetable.status` field

**AttributeConverter Implementation**:
```java
@Converter(autoApply = false)
public class ReservationStatusConverter implements AttributeConverter<ReservationStatus, String> {
    
    @Override
    public String convertToDatabaseColumn(ReservationStatus status) {
        if (status == null) return null;
        return status.name();  // "CONFIRMED", "CANCELLED", "PENDING"
    }
    
    @Override
    public ReservationStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return null;
        try {
            return ReservationStatus.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            return null;  // Handle legacy/invalid data gracefully
        }
    }
}
```

**Usage in Entity**:
```java
@Entity
public class Reservation {
    @Convert(converter = ReservationStatusConverter.class)
    @Column(nullable = false)
    private ReservationStatus status;  // Type-safe enum
}
```

**Benefits of @Convert vs @Enumerated**:
- Same type safety and readability as `@Enumerated`
- More control over conversion logic (error handling, legacy data)
- Can be applied selectively (not auto-applied)
- Consistent pattern for all status fields

---

## 5. Repository Layer (Spring Data JPA)

### 5.1 Repository Pattern

**Approach**: Spring Data JPA interfaces extending `JpaRepository`

**All Repositories** (13 total):
- `FarePolicyHistoryRepository` - `JpaRepository<FarePolicyHistory, Long>`
- `FarePolicyRepository` - `JpaRepository<FarePolicy, Long>`
- `ReservationRepository` - `JpaRepository<Reservation, Long>`
- `ReservationStatusHistoryRepository` - `JpaRepository<ReservationStatusHistory, Long>`
- `RevenueSummaryRepository` - `JpaRepository<RevenueSummary, Long>`
- `RouteAvailabilityRepository` - `JpaRepository<RouteAvailability, Long>`
- `RouteRepository` - `JpaRepository<Route, Long>`
- `RouteStatisticsRepository` - `JpaRepository<RouteStatistics, Long>`
- `RouteTimetableEntryRepository` - `JpaRepository<RouteTimetableEntry, Long>`
- `RouteTimetableRepository` - `JpaRepository<RouteTimetable, Long>`
- `StationRepository` - `JpaRepository<Station, Long>`
- `UserRepository` - `JpaRepository<User, Long>`
- `VatRateRepository` - `JpaRepository<VatRate, Long>`

**Automatic CRUD Operations**:
- `save(entity)` → `INSERT` or `UPDATE`
- `findById(id)` → `SELECT * FROM table WHERE id = ?`
- `findAll()` → `SELECT * FROM table`
- `delete(entity)` → `DELETE FROM table WHERE id = ?`
- `count()` → `SELECT COUNT(*) FROM table`

### 5.2 Custom Query Methods

#### Method Name Derivation

**Pattern**: `findByPropertyName()` - Spring Data JPA generates SQL automatically

**Examples**:

1. **StationRepository**
   - `findByName(String name)` → `SELECT * FROM stations WHERE name = ?`

2. **UserRepository**
   - `findByUsername(String username)` → `SELECT * FROM users WHERE username = ?`
   - `findByEmail(String email)` → `SELECT * FROM users WHERE email = ?`

3. **ReservationRepository**
   - `findByRouteId(Long routeId)` → `SELECT * FROM reservations WHERE route_id = ?`
   - `findByUserId(Long userId)` → `SELECT * FROM reservations WHERE user_id = ?`
   - `findByPassengerName(String passengerName)` → `SELECT * FROM reservations WHERE passenger_name = ?`

4. **RouteTimetableRepository**
   - `findByRouteId(Long routeId)` → `SELECT * FROM route_timetables WHERE route_id = ?`
   - `findByIdAndRouteId(Long id, Long routeId)` → `SELECT * FROM route_timetables WHERE id = ? AND route_id = ?`

5. **FarePolicyRepository**
   - `findByRouteIdAndPassengerCategoryAndVehicleClass(...)` → Complex query with multiple conditions

6. **RevenueSummaryRepository**
   - `findBySummaryDate(LocalDate summaryDate)` → `SELECT * FROM revenue_summary WHERE summary_date = ?`
   - `findBySummaryDateBetween(LocalDate startDate, LocalDate endDate)` → Date range query

7. **FarePolicyHistoryRepository**
   - `findByFarePolicyIdOrderByChangedAtDesc(Long farePolicyId)` → Ordered query
   - `findByRouteIdOrderByChangedAtDesc(Long routeId)` → Ordered query

**How it works**: Spring Data JPA analyzes method names and generates SQL queries automatically. Method naming conventions:
- `findBy` + PropertyName → WHERE property = ?
- `findBy` + Property1 + `And` + Property2 → WHERE property1 = ? AND property2 = ?
- `findBy` + Property + `Between` → WHERE property BETWEEN ? AND ?
- `OrderBy` + Property + `Desc` → ORDER BY property DESC

#### Custom JPQL Queries

**Pattern**: `@Query` annotation with JPQL (Java Persistence Query Language)

**Examples**:

1. **FarePolicyRepository**
   - `findActiveFarePolicy()` - JPQL query to find active fare policy for route, category, class, and date
   - `existsOverlappingActivePolicy()` - JPQL query to check for overlapping active policies

2. **VatRateRepository**
   - `findActiveVatRateForDate()` - JPQL query to find active VAT rate for a specific date
   - `existsOverlappingVatRate()` - JPQL query to check for overlapping VAT rates

**JPQL Example**:
```java
@Query("SELECT f FROM FarePolicy f WHERE f.route.id = :routeId " +
       "AND f.passengerCategory = :category " +
       "AND f.vehicleClass = :vehicleClass " +
       "AND f.status = :status " +
       "AND f.effectiveFrom <= :date " +
       "AND (f.effectiveTo IS NULL OR f.effectiveTo > :date) " +
       "ORDER BY f.effectiveFrom DESC")
Optional<FarePolicy> findActiveFarePolicy(
    @Param("routeId") Long routeId,
    @Param("category") PassengerCategory category,
    @Param("vehicleClass") VehicleClass vehicleClass,
    @Param("status") PolicyStatus status,
    @Param("date") LocalDate date
);
```

**Benefits**:
- Type-safe queries (uses entity names and properties, not table/column names)
- Compile-time validation
- Database-agnostic (works with any JPA-compliant database)

---

## 6. Service Layer (Business Logic)

### 6.1 Service Layer Responsibilities

**Services** (10 total):
- `ReservationServiceImpl` - Reservation creation, cancellation, status tracking
- `RouteServiceImpl` - Route creation, deletion, statistics management
- `StationServiceImpl` - Station updates, validation, denormalized field updates
- `UserServiceImpl` - User registration, authentication
- `TimetableServiceImpl` - Route timetable management
- `FarePolicyServiceImpl` - Fare policy creation, validation
- `VatRateServiceImpl` - VAT rate creation, validation
- `SeatAvailabilityServiceImpl` - Seat availability calculation
- `FinancialServiceImpl` - Financial summaries and revenue calculation
- `AnalyticsServiceImpl` - Analytics and reporting

### 6.2 Validation and Integrity Checks

**All validation moved from database triggers to service layer**:

1. **FarePolicyService**
   - Validates date ranges (effectiveFrom < effectiveTo)
   - Checks for overlapping active policies using `existsOverlappingActivePolicy()`
   - Location: `src/main/java/multitier/trans/service/FarePolicyServiceImpl.java`

2. **VatRateService**
   - Validates date ranges (effectiveFrom < effectiveTo)
   - Checks for overlapping rates using `existsOverlappingVatRate()`
   - Location: `src/main/java/multitier/trans/service/VatRateServiceImpl.java`

3. **StationService**
   - Validates status changes (prevents closing if active routes exist)
   - Prevents deletion if station is used in routes
   - Updates denormalized station names in reservations when station name changes
   - Location: `src/main/java/multitier/trans/service/StationServiceImpl.java`

4. **RouteService**
   - Prevents deletion if route has existing reservations
   - Creates route statistics automatically when route is created
   - Location: `src/main/java/multitier/trans/service/RouteServiceImpl.java`

### 6.3 Denormalization in Service Layer

**Pattern**: Service layer calculates and sets denormalized fields

**Example**: Reservation Denormalization

**Location**: `src/main/java/multitier/trans/service/ReservationServiceImpl.java`

**Method**: `updateDenormalizedFields(Reservation reservation, Route route)`

**What it does**:
1. Sets station names from route:
   - `reservation.setOriginStationName(route.getOriginStation().getName())`
   - `reservation.setDestinationStationName(route.getDestinationStation().getName())`

2. Calculates fare using `FareCalculationService`:
   - Gets base fare, VAT amount, total fare
   - Sets `baseFare`, `vatAmount`, `totalFare` fields

**Called when**:
- Creating a new reservation (before saving)
- Updating a reservation (if route changes)

**Benefits**:
- No database triggers needed
- Business logic in Java (testable, maintainable)
- Type-safe calculations

### 6.4 History Tracking

**Pattern**: Service layer and Entity Listeners work together

**FarePolicy History**:
- Automatic via `FarePolicyHistoryListener` (`@PostPersist`, `@PostUpdate`, `@PreRemove`)
- Location: `src/main/java/multitier/trans/model/FarePolicyHistoryListener.java`

**Reservation Status History**:
- Manual tracking in `ReservationServiceImpl.recordStatusHistory()`
- Records old status, new status, change reason
- Location: `src/main/java/multitier/trans/service/ReservationServiceImpl.java`

**Why manual for reservations**:
- `@PostUpdate` cannot access old values
- Service layer has access to both old and new status values
- More control over what gets recorded

---

## 7. Data Initialization

### 7.1 Sample Data Initialization

**Approach**: `@PostConstruct` in configuration class

**Location**: `src/main/java/multitier/trans/config/DataInitializer.java`

**How it works**:
- `@PostConstruct` method runs after Spring context is fully loaded
- Checks if tables are empty before inserting sample data
- Idempotent: safe to run multiple times

**Initialization methods**:
- `initializeStations()` - Creates sample stations
- `initializeRoutes()` - Creates sample routes
- `initializeTimetables()` - Creates sample route timetables and entries
- `initializeVatRates()` - Creates default VAT rate
- `initializeFarePolicies()` - Creates sample fare policies
- `initializeReservations()` - Creates sample reservations

**Configuration**:
```properties
app.data.initialize=true  # Set to false to disable sample data
```

**Benefits**:
- No SQL scripts needed
- Type-safe data creation (uses JPA entities)
- Conditional initialization (only if tables are empty)
- Easy to disable for production

---

## 8. Complete Data Flow Example

### Creating a Reservation

```
1. HTTP POST /api/reservations
   Request Body: {
     "routeId": 1,
     "passengerName": "John Doe",
     "seatCount": 2,
     "departureTime": "2025-01-15T10:00:00",
     "arrivalTime": "2025-01-15T11:30:00",
     "passengerCategory": "ADULT",
     "vehicleClass": "STANDARD"
   }
   ↓
2. ReservationController.createReservation(request)
   ↓
3. ReservationService.createReservation(request)
   ↓
4. Get authenticated user from SecurityContext
   ↓
5. Load Route entity: routeRepository.findById(routeId)
   → Hibernate: SELECT * FROM routes WHERE id = ?
   ↓
6. Create Reservation object using ReservationFactory
   - Sets user, route, passenger details
   - Creates TripTimeDetails (embedded value object)
   - Sets passengerCategory and vehicleClass (enums)
   - Sets status = ReservationStatus.CONFIRMED (enum with @Convert)
   ↓
7. Update denormalized fields (service layer)
   - Sets originStationName, destinationStationName from route
   - Calculates fare using FareCalculationService
   - Sets baseFare, vatAmount, totalFare
   ↓
8. reservationRepository.save(reservation)
   → Hibernate: INSERT INTO reservations (user_id, route_id, passenger_name, 
                                          departure_time, arrival_time, 
                                          passenger_category, vehicle_class, 
                                          status, origin_station_name, 
                                          destination_station_name, base_fare, 
                                          vat_amount, total_fare, ...) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ...)
   ↓
9. BaseEntityListener.prePersist() (if Reservation extended BaseEntity)
   → Sets createdAt and updatedAt timestamps
   ↓
10. ReservationStatusHistoryListener (if configured)
    → Records status change (if applicable)
    ↓
11. Update route availability (service layer)
    → Calls SeatAvailabilityService to recalculate availability
    ↓
12. Return Reservation object to controller
    → Serialized to JSON and sent as HTTP response
```

---

## 9. Key Features

### 9.1 Pure JPA/Hibernate Schema Management

- **No Flyway migrations** for schema (Flyway disabled)
- **No SQL scripts** for table creation
- **Automatic schema generation** from entity definitions
- **Schema evolution** handled automatically

### 9.2 Type-Safe Persistence

- **Java enums** for constants (type-safe, compile-time validation)
- **Entity relationships** mapped with annotations
- **Value objects** embedded in entities
- **Automatic conversion** between Java types and database types

### 9.3 Automatic Timestamp Management

- **BaseEntity** pattern for common fields
- **Entity Listeners** for automatic timestamp setting
- **Applied to all entities** extending BaseEntity

### 9.4 History/Audit Tracking

- **FarePolicyHistoryListener** - Automatic history for fare policies
- **ReservationStatusHistoryListener** - Status change tracking
- **Service layer** - Manual tracking where old values are needed

### 9.5 Service Layer Validation

- **All validation** moved from database to service layer
- **Type-safe validation** using Java code
- **Testable** validation logic
- **Custom repository queries** for complex validation checks

### 9.6 Denormalization in Service Layer

- **No database triggers** for business logic
- **Service layer** calculates and sets denormalized fields
- **Type-safe calculations** using Java
- **Maintainable** business logic

---

## 10. Configuration Summary

### application.properties

```properties
# Database Connection
spring.datasource.url=jdbc:postgresql://localhost:5438/tms_db
spring.datasource.username=tms_user
spring.datasource.password=gi

# JPA/Hibernate - Schema Management
spring.jpa.hibernate.ddl-auto=update          # Auto-create/update schema
spring.jpa.show-sql=false                    # Set to true to see SQL
spring.flyway.enabled=false                  # Flyway disabled - JPA manages schema

# Connection Pooling
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5

# Data Initialization
app.data.initialize=true                     # Enable sample data via @PostConstruct
```

---

## 11. Summary

The persistence model uses:

- **JPA/Hibernate** for ORM and schema management
- **Spring Data JPA** for repositories (automatic CRUD operations)
- **Entity Listeners** for automatic timestamp management and history tracking
- **Java Enums** for type-safe constants (with `@Enumerated` and `@Convert`)
- **Value Objects** (`@Embeddable`/`@Embedded`) for logical grouping
- **Component Entities** (`@OneToMany` with cascade and orphanRemoval) for managed child entities
- **Inheritance** (`SINGLE_TABLE` strategy) for User hierarchy
- **Service Layer** for validation, denormalization, and business logic
- **@PostConstruct** for sample data initialization

**Key Benefits**:
- ✅ Type-safe schema definition (entities define database structure)
- ✅ Automatic schema management (no manual migrations)
- ✅ Type-safe data access (Java objects, not SQL strings)
- ✅ Automatic timestamp management (via Entity Listeners)
- ✅ History/audit tracking (via Entity Listeners)
- ✅ Service layer validation (testable, maintainable)
- ✅ No database triggers for business logic (all in Java)
- ✅ Pure JPA/Hibernate approach (standard, maintainable)

This architecture provides a clean separation of concerns, type safety throughout the stack, and maintainable code that follows JPA best practices.

