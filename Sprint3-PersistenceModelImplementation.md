# Persistence Model Implementation

## Architecture Overview

The Transportation Management System uses a **layered persistence model** that bridges the gap between Java objects and PostgreSQL database tables:

```
HTTP Request → Controller → Service → Repository → JPA/Hibernate → PostgreSQL Database
```

---

## 1. Database Layer (PostgreSQL)

### Schema Design
- **Tables**: `users`, `reservations`, `routes`, `stations`, `fare_policies`, etc.
- **Relationships**: Foreign keys with constraints (`ON DELETE RESTRICT`, `ON DELETE CASCADE`)
- **Constraints**: Check constraints, unique constraints, NOT NULL
- **Indexes**: For performance on frequently queried columns

**Example from schema:**
```sql
CREATE TABLE reservations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    route_id BIGINT NOT NULL,
    passenger_name VARCHAR(100),
    ...
    CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE RESTRICT
);
```

---

## 2. Entity Model Layer (JPA Annotations)

### Purpose
Maps Java objects to database tables using JPA annotations.

### Example: `Reservation` Entity

```java
@Entity                          // Marks as JPA entity
@Table(name = "reservations")    // Maps to database table
public class Reservation {
    
    @Id                          // Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment
    private Long id;
    
    @ManyToOne                   // Foreign key relationship
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;
    
    @Column(name = "passenger_name")  // Maps to column
    private String passengerName;
    
    @Embedded                    // Value object (TripTimeDetails)
    @AttributeOverrides({...})
    private TripTimeDetails tripDetails;
}
```

### Key Annotations Explained:
- **`@Entity`**: Marks class as a JPA entity
- **`@Table(name = "...")`**: Specifies table name
- **`@Id`**: Primary key
- **`@GeneratedValue`**: Auto-generation strategy
- **`@ManyToOne`**: Many-to-one relationship (child side - has foreign key)
- **`@OneToMany`**: One-to-many relationship (parent side - has collection)
- **`@JoinColumn`**: Specifies foreign key column name
- **`@Column`**: Column mapping
- **`@Embedded`**: Value object embedding
- **`@Enumerated`**: Maps Java enum to database column (STRING or ORDINAL)
- **`@Inheritance`**: Entity inheritance strategy
- **`@DiscriminatorColumn`**: Column that distinguishes entity subclasses
- **`@DiscriminatorValue`**: Value used to identify entity subclass

---

## 3. Repository Layer (Spring Data JPA)

### Purpose
Provides data access without writing SQL.

### Example: `ReservationRepository`

```java
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    // Spring Data JPA auto-generates SQL from method name
    List<Reservation> findByRouteId(Long routeId);
    // → SQL: SELECT * FROM reservations WHERE route_id = ?
    
    List<Reservation> findByUserId(Long userId);
    // → SQL: SELECT * FROM reservations WHERE user_id = ?
    
    // Custom native query for database function
    @Query(value = "SELECT * FROM calculate_reservation_fare(...)", nativeQuery = true)
    Map<String, Object> calculateFare(...);
}
```

### What `JpaRepository` Provides Automatically:
- `save(entity)` → `INSERT` or `UPDATE`
- `findById(id)` → `SELECT * FROM table WHERE id = ?`
- `findAll()` → `SELECT * FROM table`
- `delete(entity)` → `DELETE FROM table WHERE id = ?`
- `count()` → `SELECT COUNT(*) FROM table`

---

## 4. Service Layer (Business Logic)

### Purpose
Encapsulates business logic and orchestrates data operations.

### Example: `ReservationServiceImpl`

```java
@Service
public class ReservationServiceImpl implements ReservationService {
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private UserService userService;
    
    @Override
    public Reservation createReservation(CreateReservationRequest request) {
        // 1. Get authenticated user
        User user = userService.findByUsername(username);
        
        // 2. Load related entity
        Route route = routeRepository.findById(request.getRouteId())
            .orElseThrow(() -> new RuntimeException("Route not found"));
        
        // 3. Create entity object
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setRoute(route);
        // ... set other fields
        
        // 4. Save to database (Hibernate generates INSERT)
        Reservation saved = reservationRepository.save(reservation);
        
        // 5. Refresh to get denormalized fields from triggers
        return reservationRepository.findById(saved.getId()).orElseThrow(...);
    }
}
```

---

## 5. Complete Data Flow Example: Creating a Reservation

### Step-by-Step Flow

```
1. HTTP POST /api/reservations
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
6. Create Reservation object and set fields
   ↓
7. reservationRepository.save(reservation)
   → Hibernate: INSERT INTO reservations (user_id, route_id, ...) VALUES (...)
   ↓
8. Database triggers fire:
   - update_reservation_denormalized_fields() 
     → Calculates base_fare, vat_amount, total_fare
     → Copies origin_station_name, destination_station_name
   - validate_seat_capacity()
     → Checks seat availability
   ↓
9. Refresh entity: reservationRepository.findById(savedId)
   → Hibernate: SELECT * FROM reservations WHERE id = ?
   → Returns entity with denormalized fields populated
   ↓
10. Return Reservation object to controller
    → Serialized to JSON and sent as HTTP response
```

---

## 6. Configuration

### `application.properties`

```properties
# Database connection
spring.datasource.url=jdbc:postgresql://localhost:5438/tms_db
spring.datasource.username=tms_user
spring.datasource.password=gi

# JPA/Hibernate settings
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate  # Validates schema, doesn't create tables
spring.jpa.show-sql=false               # Set to true to see generated SQL

# Connection pooling (HikariCP)
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
```

### Key Settings Explained:
- **`ddl-auto=validate`**: Validates schema matches entities (doesn't auto-create)
- **`show-sql=false`**: Set to `true` to see generated SQL in logs
- **Connection Pooling**: HikariCP manages database connections efficiently

---

## 7. Advanced Features

### A. Relationship Mapping

JPA provides annotations to map relationships between entities. The system uses both **`@ManyToOne`** and **`@OneToMany`** to represent bidirectional relationships.

#### `@ManyToOne` (Many-to-One Relationship)

Represents the "many" side of a relationship. Each entity has a reference to a single related entity.

**Example: Reservation → User**
```java
@Entity
@Table(name = "reservations")
public class Reservation {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // Many reservations belong to one user
}
```

**Example: RouteTimetableEntry → RouteTimetable**
```java
@Entity
@Table(name = "route_timetable_entries")
public class RouteTimetableEntry {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timetable_id", nullable = false)
    private RouteTimetable timetable;  // Many entries belong to one timetable
}
```

**Key Points:**
- **`@JoinColumn`**: Specifies the foreign key column name in the database
- **`fetch = FetchType.LAZY`**: Related entity loaded only when accessed (performance optimization)
- **Database**: Creates a foreign key column (`user_id`, `timetable_id`)

#### `@OneToMany` (One-to-Many Relationship)

Represents the "one" side of a relationship. One entity has a collection of related entities. When child entities are managed as components of the parent, this creates a **Component Entity Relationship**.

**Component Entities** are child entities that:
- Have no independent existence outside their parent
- Are managed entirely by the parent entity
- Are typically deleted when removed from the parent's collection
- Represent parts/components of the parent entity

**Example: RouteTimetable → RouteTimetableEntry (Component Entity Relationship)**
```java
@Entity
@Table(name = "route_timetables")
public class RouteTimetable {
    
    @OneToMany(mappedBy = "timetable", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RouteTimetableEntry> entries = new ArrayList<>();
    // One timetable has many entries
}
```

**Key Parameters Explained:**

1. **`mappedBy = "timetable"`**: 
   - Indicates this is the **inverse side** (non-owning side) of the relationship
   - The **owning side** (root entity) is `RouteTimetableEntry.timetable` - it has the `@JoinColumn`
   - The foreign key is managed by the `@ManyToOne` side (`RouteTimetableEntry.timetable`)
   - No `@JoinColumn` needed here (it's on the `@ManyToOne` side)
   - **Root Entity Concept**: The entity with `@JoinColumn` is the "owning side" that manages the foreign key

2. **`cascade = CascadeType.ALL`**:
   - Operations on the parent (`RouteTimetable`) cascade to children (`RouteTimetableEntry`)
   - When you save/update/delete a timetable, entries are automatically saved/updated/deleted
   - Prevents manual management of child entities
   - **CascadeType Options**:
     - `CascadeType.ALL`: All operations cascade (PERSIST, MERGE, REMOVE, REFRESH, DETACH)
     - `CascadeType.PERSIST`: Save operations cascade
     - `CascadeType.MERGE`: Update operations cascade
     - `CascadeType.REMOVE`: Delete operations cascade
     - `CascadeType.REFRESH`: Refresh operations cascade
     - `CascadeType.DETACH`: Detach operations cascade

3. **`orphanRemoval = true`**:
   - If an entry is removed from the collection, it's automatically deleted from the database
   - Ensures no orphaned entries exist without a parent timetable
   - **Difference from CascadeType.REMOVE**:
     - `CascadeType.REMOVE`: Deletes children when parent is deleted
     - `orphanRemoval = true`: Deletes children when removed from collection (even if parent still exists)

4. **`fetch` (implicit or explicit)**:
   - **Default for `@OneToMany`**: `FetchType.LAZY` (lazy loading)
   - Related entities loaded only when accessed (performance optimization)
   - **FetchType Options**:
     - `FetchType.LAZY`: Load on demand (default for `@OneToMany`)
     - `FetchType.EAGER`: Load immediately (default for `@ManyToOne`, `@OneToOne`)
   - **Best Practice**: Use `LAZY` for collections to avoid N+1 query problems

**Bidirectional Relationship Pattern:**

```java
// Parent side (One) - INVERSE SIDE (non-owning)
@Entity
public class RouteTimetable {
    @OneToMany(mappedBy = "timetable", cascade = CascadeType.ALL, orphanRemoval = true)
    // mappedBy indicates this is the inverse side
    // No @JoinColumn here - foreign key is on the child side
    private List<RouteTimetableEntry> entries = new ArrayList<>();
}

// Child side (Many) - OWNING SIDE (root entity for this relationship)
@Entity
public class RouteTimetableEntry {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timetable_id", nullable = false)
    // @JoinColumn makes this the owning side - manages the foreign key
    private RouteTimetable timetable;
}
```

**Owning Side vs Inverse Side (Root Entity):**

- **Owning Side** (`RouteTimetableEntry`):
  - Has the `@JoinColumn` annotation
  - Manages the foreign key column in the database
  - Changes to the relationship are tracked here
  - This is the "root entity" for the relationship mapping

- **Inverse Side** (`RouteTimetable`):
  - Has `mappedBy` attribute
  - References the owning side's field name
  - Does NOT manage the foreign key
  - Changes here don't affect the database relationship

**Database Schema:**
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

**Usage Example:**
```java
// Create a timetable with entries
RouteTimetable timetable = new RouteTimetable();
timetable.setName("Weekday Schedule");

// Add entries (cascade saves them automatically)
RouteTimetableEntry mondayEntry = new RouteTimetableEntry();
mondayEntry.setServiceDay(DayOfWeek.MONDAY);
mondayEntry.setDepartureTime(LocalTime.of(8, 0));
timetable.addEntry(mondayEntry);  // Sets bidirectional relationship

RouteTimetableEntry tuesdayEntry = new RouteTimetableEntry();
tuesdayEntry.setServiceDay(DayOfWeek.TUESDAY);
timetable.addEntry(tuesdayEntry);

// Save timetable - entries are automatically saved (cascade)
timetableRepository.save(timetable);
// → INSERT INTO route_timetables (...) VALUES (...)
// → INSERT INTO route_timetable_entries (timetable_id, ...) VALUES (1, ...)
// → INSERT INTO route_timetable_entries (timetable_id, ...) VALUES (1, ...)
```

**Complete Configuration Example:**

```java
@Entity
@Table(name = "route_timetables")
public class RouteTimetable {
    
    // Root entity relationship configuration
    @OneToMany(
        mappedBy = "timetable",           // Inverse side - references owning side
        cascade = CascadeType.ALL,        // All operations cascade to children
        orphanRemoval = true,             // Remove orphans when removed from collection
        fetch = FetchType.LAZY           // Lazy loading (default, but explicit is clearer)
    )
    private List<RouteTimetableEntry> entries = new ArrayList<>();
    
    // Helper method to maintain bidirectional relationship
    public void addEntry(RouteTimetableEntry entry) {
        entries.add(entry);
        entry.setTimetable(this);  // Set owning side
    }
    
    public void removeEntry(RouteTimetableEntry entry) {
        entries.remove(entry);
        entry.setTimetable(null);  // Clear owning side
        // With orphanRemoval = true, entry will be deleted from DB
    }
}
```

**When to Use This Pattern:**

✅ **Use `cascade = CascadeType.ALL` + `orphanRemoval = true` when:**
- Child entities have no independent existence (composition relationship)
- Children should be managed entirely by the parent
- Example: `RouteTimetableEntry` only exists within a `RouteTimetable`

✅ **Use `mappedBy` when:**
- You want a bidirectional relationship
- The child entity should own the foreign key
- You want to avoid duplicate foreign key columns

✅ **Use `fetch = FetchType.LAZY` when:**
- Collections might be large
- You don't always need to load children
- Performance is important

**Benefits:**
- **Automatic Management**: Cascade operations handle child entities automatically
- **Data Integrity**: Orphan removal prevents orphaned records
- **Bidirectional Navigation**: Can navigate from parent to children and vice versa
- **Lazy Loading**: Related entities loaded only when needed (performance)
- **Root Entity Control**: Parent entity controls the lifecycle of children

### B. Denormalization
- `reservations` table stores cached values (`base_fare`, `vat_amount`, `origin_station_name`)
- Populated by database triggers for performance
- Avoids joins when reading reservations

### C. Value Objects vs Component Entities

The system uses two different patterns for grouping related data:

#### Value Objects (`@Embedded` / `@Embeddable`)

Value objects are **embedded** into the parent entity's table. They don't have their own table or identity.

```java
// Value Object - @Embeddable
@Embeddable
public class TripTimeDetails {
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
}

// Used in parent entity - @Embedded
@Entity
@Table(name = "reservations")
public class Reservation {
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "departureTime", column = @Column(name = "departure_time")),
        @AttributeOverride(name = "arrivalTime", column = @Column(name = "arrival_time"))
    })
    private TripTimeDetails tripDetails;  // Stored in same table, different columns
}
```

**Database Storage:**
```sql
-- Value object fields stored in parent table
CREATE TABLE reservations (
    id BIGSERIAL PRIMARY KEY,
    departure_time TIMESTAMP NOT NULL,  -- From TripTimeDetails
    arrival_time TIMESTAMP NOT NULL,    -- From TripTimeDetails
    ...
);
```

**Characteristics:**
- ✅ Stored in the same table as parent
- ✅ No separate identity (no `@Id`)
- ✅ Cannot be shared between entities
- ✅ Simple value grouping

#### Component Entities (`@OneToMany`)

Component entities are **separate entities** with their own table and identity, but are managed as components of the parent.

```java
// Parent Entity
@Entity
@Table(name = "route_timetables")
public class RouteTimetable {
    @OneToMany(mappedBy = "timetable", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RouteTimetableEntry> entries = new ArrayList<>();
    // Component entities - managed as parts of the timetable
}

// Component Entity - separate entity with own identity
@Entity
@Table(name = "route_timetable_entries")
public class RouteTimetableEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Has own identity
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timetable_id", nullable = false)
    private RouteTimetable timetable;
    
    private DayOfWeek serviceDay;
    private LocalTime departureTime;
    // ...
}
```

**Database Storage:**
```sql
-- Parent table
CREATE TABLE route_timetables (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    ...
);

-- Component entity table (separate table)
CREATE TABLE route_timetable_entries (
    id BIGSERIAL PRIMARY KEY,  -- Has own identity
    timetable_id BIGINT NOT NULL,  -- Foreign key to parent
    service_day VARCHAR(10) NOT NULL,
    departure_time TIME NOT NULL,
    ...
    CONSTRAINT fk_entry_timetable FOREIGN KEY (timetable_id) 
        REFERENCES route_timetables(id) ON DELETE CASCADE
);
```

**Characteristics:**
- ✅ Stored in separate table
- ✅ Has own identity (`@Id`)
- ✅ Can be queried independently (if needed)
- ✅ Managed as component via cascade and orphanRemoval

#### Comparison: Value Objects vs Component Entities

| Aspect | Value Objects (`@Embedded`) | Component Entities (`@OneToMany`) |
|--------|----------------------------|-----------------------------------|
| **Storage** | Same table as parent | Separate table |
| **Identity** | No `@Id` | Has `@Id` |
| **Query** | Cannot query independently | Can query independently |
| **Relationship** | Embedded columns | Foreign key relationship |
| **Use Case** | Simple value grouping | Complex child entities |
| **Example** | `TripTimeDetails` in `Reservation` | `RouteTimetableEntry` in `RouteTimetable` |

**When to Use Each:**

- **Use Value Objects (`@Embedded`)** when:
  - Data is simple and doesn't need independent identity
  - Data is always accessed with the parent
  - No need to query children separately
  - Example: Time details, address, money amount

- **Use Component Entities (`@OneToMany`)** when:
  - Children have complex structure
  - Children might need to be queried independently
  - Children have relationships to other entities
  - Need to maintain referential integrity
  - Example: Timetable entries, order items, reservation details

### D. Enums (Type-Safe Constants)

The system uses **Java enums** for type-safe constants that map to database columns:

**Enum Definitions:**
```java
// PassengerCategory.java
public enum PassengerCategory {
    ADULT,
    CHILD,
    SENIOR,
    STUDENT
}

// VehicleClass.java
public enum VehicleClass {
    STANDARD,
    COACH,
    MINI_BUS,
    DOUBLE_DECKER
}
```

**Usage in Entities:**
```java
@Entity
@Table(name = "reservations")
public class Reservation {
    
    @NotNull
    @Enumerated(EnumType.STRING)  // Stores "ADULT" in DB, not 0
    @Column(name = "passenger_category", nullable = false)
    private PassengerCategory passengerCategory;
    
    @NotNull
    @Enumerated(EnumType.STRING)  // Stores "STANDARD" in DB, not 0
    @Column(name = "vehicle_class", nullable = false)
    private VehicleClass vehicleClass;
}
```

**How `@Enumerated(EnumType.STRING)` Works:**

1. **Database Storage**: Enum values stored as readable strings (`"ADULT"`, `"STANDARD"`) instead of numbers
2. **Automatic Conversion**: JPA automatically converts between Java enum and database string
3. **Type Safety**: Compiler ensures only valid enum values are used
4. **JSON Serialization**: Spring automatically converts JSON strings to enum values in API requests

**Database Schema:**
```sql
-- Enum values stored as VARCHAR in database
CREATE TABLE reservations (
    ...
    passenger_category VARCHAR(20) NOT NULL,
    vehicle_class VARCHAR(20) NOT NULL,
    ...
    CONSTRAINT fk_reservation_category FOREIGN KEY (passenger_category) 
        REFERENCES passenger_categories(category) ON DELETE RESTRICT,
    CONSTRAINT fk_reservation_vehicle_class FOREIGN KEY (vehicle_class) 
        REFERENCES vehicle_classes(class) ON DELETE RESTRICT
);
```

**Benefits:**
- **Type Safety**: Compile-time validation prevents invalid values
- **Readability**: Database values are human-readable strings
- **Refactoring**: IDE can safely rename enum values
- **API Integration**: Automatic JSON conversion in REST endpoints
- **Database Integrity**: Foreign key constraints ensure referential integrity

**Example Flow:**
```java
// 1. Client sends JSON
{
  "passengerCategory": "ADULT",
  "vehicleClass": "STANDARD"
}

// 2. Spring converts JSON string → Enum
CreateReservationRequest.getPassengerCategory() → PassengerCategory.ADULT

// 3. Entity stores enum
reservation.setPassengerCategory(PassengerCategory.ADULT);

// 4. JPA saves as string
// → INSERT INTO reservations (passenger_category, ...) 
//   VALUES ('ADULT', ...)
```

### E. Entity Inheritance (JPA Inheritance)

The system uses **JPA SINGLE_TABLE inheritance** for the User entity hierarchy:

```java
// Base User entity
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("USER")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String email;
    private String passwordHash;
    private String role;  // Determined by entity type
    
    // Common fields for all user types
}

// Regular User subclass
@Entity
@DiscriminatorValue("USER")
public class RegularUser extends User {
    @Override
    public String getRole() {
        return "USER";
    }
}

// Admin User subclass
@Entity
@DiscriminatorValue("ADMIN")
public class AdminUser extends User {
    @Override
    public String getRole() {
        return "ADMIN";
    }
}
```

**How SINGLE_TABLE Inheritance Works:**

1. **Single Table**: All user types stored in one `users` table
2. **Discriminator Column**: `user_type` column distinguishes between subclasses
3. **No Joins**: Efficient queries - no table joins needed
4. **Polymorphism**: Can work with `User` base class or specific subclasses

**Database Schema:**
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    user_type VARCHAR(20) NOT NULL DEFAULT 'USER',  -- Discriminator column
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    -- ... other common fields
);
```

**Usage Example:**
```java
// Creating a regular user
RegularUser user = new RegularUser("john", "john@example.com", "hashedPassword");
userRepository.save(user);
// → INSERT INTO users (user_type, username, email, ...) 
//   VALUES ('USER', 'john', 'john@example.com', ...)

// Creating an admin user
AdminUser admin = new AdminUser("admin", "admin@example.com", "hashedPassword");
userRepository.save(admin);
// → INSERT INTO users (user_type, username, email, ...) 
//   VALUES ('ADMIN', 'admin', 'admin@example.com', ...)

// Querying - JPA automatically filters by discriminator
User found = userRepository.findByUsername("admin");
// → Returns AdminUser instance (user_type = 'ADMIN')
```

**Benefits:**
- **Type Safety**: `AdminUser` and `RegularUser` are distinct types
- **Polymorphism**: Can work with `User` base class
- **Extensibility**: Easy to add new user types (e.g., `ManagerUser`)
- **Performance**: Single table, no joins required
- **Backward Compatible**: Existing code continues to work

### F. Automatic SQL Generation
- Spring Data JPA generates SQL from method names
- `findByUserId(Long id)` → `SELECT * FROM reservations WHERE user_id = ?`

### G. Database Functions
- Can call PostgreSQL functions via `@Query(nativeQuery = true)`
- Example: `calculate_reservation_fare()` function

### H. Database Indexes

**Indexes** are database structures that improve query performance by creating a sorted data structure that allows fast lookups. They're essential for optimizing frequently queried columns.

#### What Are Indexes?

An index is like a book's index - instead of scanning every page, the database can quickly jump to the relevant data. Without indexes, queries must perform **full table scans**, which become slow as data grows.

#### Types of Indexes in the System

**1. Single Column Indexes**

Indexes on frequently queried individual columns:

```sql
-- User authentication lookups
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);

-- User role filtering
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_type ON users(user_type);

-- Station lookups
CREATE INDEX idx_stations_name ON stations(name);
CREATE INDEX idx_stations_status ON stations(status);

-- Route filtering
CREATE INDEX idx_routes_origin ON routes(origin_station_id);
CREATE INDEX idx_routes_destination ON routes(destination_station_id);
CREATE INDEX idx_routes_status ON routes(status);

-- Reservation queries
CREATE INDEX idx_reservations_user ON reservations(user_id);
CREATE INDEX idx_reservations_route ON reservations(route_id);
CREATE INDEX idx_reservations_status ON reservations(status);
CREATE INDEX idx_reservations_departure_time ON reservations(departure_time);
```

**2. Composite Indexes (Multiple Columns)**

Indexes on multiple columns used together in queries:

```sql
-- Route origin-destination lookups
CREATE INDEX idx_routes_origin_destination ON routes(origin_station_id, destination_station_id);

-- User reservations by status
CREATE INDEX idx_reservations_user_status ON reservations(user_id, status);

-- Route reservations by status
CREATE INDEX idx_reservations_route_status ON reservations(route_id, status);

-- Fare policy lookups (multiple criteria)
CREATE INDEX idx_fare_policies_lookup ON fare_policies(
    route_id, 
    passenger_category, 
    vehicle_class, 
    status, 
    effective_from, 
    effective_to
);

-- Route availability queries
CREATE INDEX idx_route_availability_route_departure ON route_availability(route_id, departure_time);
```

**3. Partial Indexes (Conditional)**

Indexes that only include rows matching a condition:

```sql
-- Only index available routes (WHERE clause)
CREATE INDEX idx_route_availability_available 
    ON route_availability(available_seats) 
    WHERE available_seats > 0;
```

This index only includes routes with available seats, making "find available routes" queries faster.

**4. Unique Indexes**

Ensure uniqueness while providing fast lookups:

```sql
-- Ensure only one active fare policy per route/category/class combination
CREATE UNIQUE INDEX idx_fare_policies_active_unique 
    ON fare_policies(route_id, passenger_category, vehicle_class) 
    WHERE status = 'ACTIVE' AND effective_to IS NULL;
```

#### How Indexes Improve Performance

**Example: Finding User Reservations**

```sql
-- Query: Find all reservations for user_id = 123
SELECT * FROM reservations WHERE user_id = 123;
```

**Without Index:**
- Database scans **every row** in `reservations` table
- Time complexity: O(n) - linear scan
- With 1 million reservations: checks all 1 million rows

**With Index (`idx_reservations_user`):**
- Database uses index to jump directly to user_id = 123 entries
- Time complexity: O(log n) - binary search
- With 1 million reservations: checks ~20 rows (log₂(1,000,000) ≈ 20)

**Performance Improvement:** 50,000x faster!

#### Index Usage in JPA/Hibernate

JPA/Hibernate **automatically benefits** from database indexes:

```java
// This query uses idx_reservations_user automatically
List<Reservation> reservations = reservationRepository.findByUserId(123L);
// → SQL: SELECT * FROM reservations WHERE user_id = 123
// → PostgreSQL uses idx_reservations_user for fast lookup
```

**Foreign Key Columns:**
- Foreign keys often have indexes automatically (depending on database)
- Example: `user_id` in `reservations` table
- Used for JOIN operations and foreign key lookups

#### Index Strategy in the System

**Indexes are created for:**

1. **Foreign Key Columns**: Fast JOINs and relationship queries
   ```sql
   CREATE INDEX idx_reservations_user ON reservations(user_id);
   CREATE INDEX idx_reservations_route ON reservations(route_id);
   ```

2. **Frequently Filtered Columns**: WHERE clause columns
   ```sql
   CREATE INDEX idx_reservations_status ON reservations(status);
   CREATE INDEX idx_routes_status ON routes(status);
   ```

3. **Search Columns**: Columns used in lookups
   ```sql
   CREATE INDEX idx_users_username ON users(username);
   CREATE INDEX idx_stations_name ON stations(name);
   ```

4. **Composite Queries**: Multiple columns used together
   ```sql
   CREATE INDEX idx_reservations_user_status ON reservations(user_id, status);
   ```

5. **Time-Based Queries**: Date/time filtering
   ```sql
   CREATE INDEX idx_reservations_departure_time ON reservations(departure_time);
   CREATE INDEX idx_reservations_created_at ON reservations(created_at);
   ```

#### Index Trade-offs

**Benefits:**
- ✅ **Faster Queries**: Dramatically improves SELECT performance
- ✅ **Faster JOINs**: Speeds up relationship queries
- ✅ **Faster Sorting**: Indexes are pre-sorted

**Costs:**
- ❌ **Storage**: Indexes require additional disk space
- ❌ **Write Performance**: INSERT/UPDATE/DELETE operations must update indexes
- ❌ **Maintenance**: Indexes need to be maintained as data changes

**Best Practice:**
- Create indexes on columns used in WHERE, JOIN, and ORDER BY clauses
- Don't over-index: Too many indexes slow down writes
- Monitor query performance to identify missing indexes

#### Example: Index Impact on Query Performance

```sql
-- Query: Find all active reservations for a user
SELECT * FROM reservations 
WHERE user_id = 123 AND status = 'CONFIRMED'
ORDER BY departure_time;

-- Uses composite index: idx_reservations_user_status
-- Also uses: idx_reservations_departure_time (for ORDER BY)
-- Result: Fast query even with millions of reservations
```

---

## 8. Benefits of This Architecture

1. **Type Safety**: Java objects instead of raw SQL
2. **Less Boilerplate**: Spring Data JPA reduces code
3. **Automatic Mapping**: Hibernate handles object-relational mapping
4. **Transaction Management**: Spring handles transactions
5. **Lazy Loading**: Related entities loaded on demand
6. **Caching**: Hibernate first-level cache
7. **Validation**: Bean Validation annotations

---

## Summary

The persistence model uses:
- **PostgreSQL** for storage
- **JPA/Hibernate** for ORM
- **Spring Data JPA** for repositories
- **Service layer** for business logic
- **Database triggers** for denormalization
- **Database indexes** for query performance optimization
- **Java Enums** for type-safe constants (PassengerCategory, VehicleClass)
- **JPA Inheritance** for User entity hierarchy (SINGLE_TABLE strategy)

This provides type safety, reduces boilerplate, and maintains data integrity while keeping the code maintainable. The inheritance pattern allows for clean separation of user types while maintaining efficient database queries. Enums ensure compile-time validation and readable database values. Indexes optimize query performance for frequently accessed columns and relationships.

