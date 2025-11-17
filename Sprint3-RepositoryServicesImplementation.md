# Repository Services Implementation

## Overview

The Transportation Management System uses the **Repository-Service Pattern**, a layered architecture that separates data access (Repository) from business logic (Service). This pattern provides clean separation of concerns, testability, and maintainability.

```
Controller → Service → Repository → Database
   (API)    (Logic)   (Data Access)
```

---

## 1. Repository Layer (Data Access)

### What is a Repository?

A **Repository** is an abstraction over data access. It provides a collection-like interface for accessing domain objects without exposing database implementation details.

### Spring Data JPA Repository

In this application, repositories extend `JpaRepository<Entity, ID>`, which provides automatic CRUD operations without writing any SQL.

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

### Key Features:

1. **No Implementation Required**: Spring Data JPA automatically creates implementations
2. **Method Name → SQL**: Spring generates SQL from method names
3. **Built-in CRUD**: `save()`, `findById()`, `findAll()`, `delete()`, etc.
4. **Custom Queries**: Use `@Query` annotation for complex queries

### Automatic Methods from `JpaRepository`:

```java
// All these methods are available automatically:
reservationRepository.save(reservation);        // INSERT or UPDATE
reservationRepository.findById(id);             // SELECT * WHERE id = ?
reservationRepository.findAll();                // SELECT * FROM reservations
reservationRepository.delete(reservation);      // DELETE WHERE id = ?
reservationRepository.count();                  // SELECT COUNT(*) FROM reservations
reservationRepository.existsById(id);           // SELECT COUNT(*) WHERE id = ? > 0
```

### Query Method Naming Convention:

Spring Data JPA generates queries based on method names:

| Method Name | Generated SQL |
|------------|---------------|
| `findByRouteId(Long id)` | `SELECT * FROM reservations WHERE route_id = ?` |
| `findByUserIdAndStatus(Long userId, String status)` | `SELECT * FROM reservations WHERE user_id = ? AND status = ?` |
| `findByName(String name)` | `SELECT * FROM stations WHERE name = ?` |
| `findByRouteIdOrderByCreatedAtDesc(Long id)` | `SELECT * FROM reservations WHERE route_id = ? ORDER BY created_at DESC` |

### Example: `StationRepository`

```java
@Repository
public interface StationRepository extends JpaRepository<Station, Long> {
    
    // Custom query method - Spring generates: SELECT * FROM stations WHERE name = ?
    Station findByName(String name);
}
```

### Example: `FarePolicyRepository`

```java
@Repository
public interface FarePolicyRepository extends JpaRepository<FarePolicy, Long> {
    
    // Complex query with multiple parameters
    Optional<FarePolicy> findByRouteIdAndPassengerCategoryAndVehicleClass(
            Long routeId,
            PassengerCategory passengerCategory,
            VehicleClass vehicleClass
    );
    // → SQL: SELECT * FROM fare_policies 
    //        WHERE route_id = ? AND passenger_category = ? AND vehicle_class = ?
}
```

### Example: `UserRepository` (with JPA Inheritance)

The `UserRepository` works with JPA inheritance using the `SINGLE_TABLE` strategy. It can query both `RegularUser` and `AdminUser` entities through the base `User` entity:

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // These methods work for both RegularUser and AdminUser
    Optional<User> findByUsername(String username);
    // → SQL: SELECT * FROM users WHERE username = ?
    
    Optional<User> findByEmail(String email);
    // → SQL: SELECT * FROM users WHERE email = ?
    
    boolean existsByUsername(String username);
    // → SQL: SELECT COUNT(*) > 0 FROM users WHERE username = ?
    
    boolean existsByEmail(String email);
    // → SQL: SELECT COUNT(*) > 0 FROM users WHERE email = ?
}
```

**Key Points:**
- The repository is defined for the base `User` entity
- Spring Data JPA automatically handles the `user_type` discriminator column
- Queries return the appropriate subclass (`RegularUser` or `AdminUser`) based on `user_type`
- All user types are stored in the same `users` table

---

## 2. Service Layer (Business Logic)

### What is a Service?

A **Service** contains business logic and orchestrates operations between repositories. It acts as a facade, providing a clean API for controllers.

### Service Pattern Structure:

```java
// 1. Interface (Contract)
public interface ReservationService {
    Reservation createReservation(CreateReservationRequest request);
    List<Reservation> getMyReservations();
}

// 2. Implementation (Business Logic)
@Service
public class ReservationServiceImpl implements ReservationService {
    
    private final ReservationRepository reservationRepository;
    private final RouteRepository routeRepository;
    private final UserService userService;
    
    @Autowired
    public ReservationServiceImpl(...) {
        // Dependency injection
    }
    
    @Override
    public Reservation createReservation(CreateReservationRequest request) {
        // Business logic here
    }
}
```

### Key Responsibilities:

1. **Business Logic**: Validations, calculations, workflows
2. **Orchestration**: Coordinates multiple repositories
3. **Transaction Management**: Ensures data consistency
4. **Security**: Access control and authorization
5. **Error Handling**: Converts exceptions to meaningful errors

---

## 3. Repository-Service Interaction Examples

### Example 1: Simple Service (StationService)

```java
@Service
public class StationServiceImpl implements StationService {
    
    private final StationRepository stationRepository;
    
    @Autowired
    public StationServiceImpl(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }
    
    @Override
    public Station createStation(Station station) {
        // Simple pass-through with potential validation
        return stationRepository.save(station);
    }
    
    @Override
    public List<Station> getAllStations() {
        // Direct repository call
        return stationRepository.findAll();
    }
}
```

**Flow:**
```
Controller → StationService.createStation() → StationRepository.save() → Database
```

### Example 1.5: User Service (with JPA Inheritance)

The `UserService` demonstrates how services work with JPA inheritance to create different user types:

```java
@Service
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional
    public User register(RegisterRequest request) {
        // Validation
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        // Create RegularUser (inherits from User)
        // The repository saves it, and JPA sets user_type = 'USER'
        RegularUser user = new RegularUser(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        );
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        
        // Repository automatically handles inheritance
        return userRepository.save(user);
    }
    
    @Override
    public User createAdminUser(String username, String email, String password) {
        // Create AdminUser (inherits from User)
        // The repository saves it, and JPA sets user_type = 'ADMIN'
        AdminUser adminUser = new AdminUser(
                username,
                email,
                passwordEncoder.encode(password)
        );
        
        // Same repository, different entity type
        return userRepository.save(adminUser);
    }
    
    @Override
    public User findByUsername(String username) {
        // Repository returns the correct subclass based on user_type
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
```

**Flow:**
```
Controller → UserService.register()
    ↓
    → UserRepository.existsByUsername() [validation]
    → Create RegularUser entity
    → UserRepository.save(regularUser)
    → JPA sets user_type = 'USER' automatically
    ↓
Database (users table with user_type discriminator)
```

**Key Points:**
- Service creates specific subclasses (`RegularUser`, `AdminUser`)
- Repository is defined for base `User` class but handles all subclasses
- JPA automatically manages the `user_type` discriminator column
- `findByUsername()` returns the correct subclass instance

### Example 2: Complex Service (RouteService)

```java
@Service
public class RouteServiceImpl implements RouteService {
    
    private final RouteRepository routeRepository;
    private final StationRepository stationRepository;  // Multiple repositories!
    
    @Override
    public Route createRoute(CreateRouteRequest request) {
        // 1. Business Logic: Find origin station
        Station origin = stationRepository.findById(request.getOriginStationId())
                .orElseThrow(() -> new RuntimeException("Station not found"));
        
        // 2. Business Logic: Find destination station
        Station destination = stationRepository.findById(request.getDestinationStationId())
                .orElseThrow(() -> new RuntimeException("Station not found"));
        
        // 3. Business Logic: Validation
        if (origin.getId().equals(destination.getId())) {
            throw new RuntimeException("Origin and destination cannot be the same");
        }
        
        // 4. Create entity
        Route newRoute = new Route(origin, destination, request.getVehicleCapacity());
        
        // 5. Save via repository
        return routeRepository.save(newRoute);
    }
}
```

**Flow:**
```
Controller → RouteService.createRoute()
    ↓
    → StationRepository.findById() [origin]
    → StationRepository.findById() [destination]
    → Validation logic
    → RouteRepository.save()
    ↓
Database
```

### Example 3: Advanced Service (ReservationService)

```java
@Service
public class ReservationServiceImpl implements ReservationService {
    
    private final ReservationRepository reservationRepository;
    private final RouteRepository routeRepository;
    private final UserService userService;  // Can use other services!
    
    @Override
    public Reservation createReservation(CreateReservationRequest request) {
        // 1. Get authenticated user (from another service)
        User user = userService.findByUsername(username);
        
        // 2. Load route entity
        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new RuntimeException("Route not found"));
        
        // 3. Business Logic: Set passenger details with defaults
        String passengerName = request.getPassengerName();
        if (passengerName == null || passengerName.trim().isEmpty()) {
            passengerName = user.getFirstName() + " " + user.getLastName();
        }
        
        // 4. Create entity
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setRoute(route);
        reservation.setPassengerName(passengerName);
        // ... set other fields
        
        // 5. Save via repository
        Reservation saved = reservationRepository.save(reservation);
        
        // 6. Refresh to get denormalized fields from database triggers
        return reservationRepository.findById(saved.getId()).orElseThrow(...);
    }
    
    @Override
    public List<Reservation> getMyReservations() {
        // 1. Get current user
        User user = userService.findByUsername(username);
        
        // 2. Use custom repository method
        return reservationRepository.findByUserId(user.getId());
    }
    
    @Override
    public FareCalculationResponse calculateFare(...) {
        // Call database function via repository
        Map<String, Object> result = reservationRepository.calculateFare(...);
        
        // Transform result to DTO
        return new FareCalculationResponse(...);
    }
}
```

**Flow:**
```
Controller → ReservationService.createReservation()
    ↓
    → UserService.findByUsername() [get authenticated user]
    → RouteRepository.findById() [get route]
    → Business logic [set defaults, validations]
    → ReservationRepository.save() [persist]
    → ReservationRepository.findById() [refresh with triggers]
    ↓
Database
```

---

## 4. Dependency Injection

### How Services Get Repositories

Spring uses **Dependency Injection** to provide repositories to services:

```java
@Service
public class ReservationServiceImpl implements ReservationService {
    
    // Fields are marked as final (immutable)
    private final ReservationRepository reservationRepository;
    private final RouteRepository routeRepository;
    
    // Constructor injection (recommended)
    @Autowired
    public ReservationServiceImpl(
            ReservationRepository reservationRepository,
            RouteRepository routeRepository) {
        this.reservationRepository = reservationRepository;
        this.routeRepository = routeRepository;
    }
}
```

### Why Constructor Injection?

1. **Immutability**: Fields can be `final`
2. **Testability**: Easy to mock in unit tests
3. **Required Dependencies**: Compiler ensures all dependencies are provided
4. **No Reflection**: Better performance than field injection

---

## 5. Transaction Management

### Automatic Transactions

Spring automatically manages transactions for service methods:

```java
@Service
public class ReservationServiceImpl {
    
    @Override
    public Reservation createReservation(...) {
        // This entire method runs in a transaction
        // If any step fails, everything rolls back
        
        Route route = routeRepository.findById(...);
        Reservation reservation = new Reservation(...);
        return reservationRepository.save(reservation);
    }
}
```

### Transaction Behavior:

- **All or Nothing**: If any operation fails, the entire transaction rolls back
- **Automatic**: No need to manually start/commit transactions
- **Isolation**: Each transaction sees a consistent view of data

---

## 6. Custom Repository Queries

### Method Name Queries (Automatic)

```java
// Spring generates: SELECT * FROM reservations WHERE user_id = ? AND status = ?
List<Reservation> findByUserIdAndStatus(Long userId, String status);

// Spring generates: SELECT * FROM stations WHERE name LIKE ?
List<Station> findByNameContaining(String name);
```

### @Query Annotation (Custom SQL)

```java
@Query(value = "SELECT * FROM calculate_reservation_fare(:routeId, :passengerCategory, :seatCount, :reservationDate)", 
       nativeQuery = true)
Map<String, Object> calculateFare(
        @Param("routeId") Integer routeId,
        @Param("passengerCategory") String passengerCategory,
        @Param("seatCount") Integer seatCount,
        @Param("reservationDate") LocalDateTime reservationDate
);
```

### JPQL Queries (Java Persistence Query Language)

```java
@Query("SELECT r FROM Reservation r WHERE r.user.id = :userId AND r.status = :status")
List<Reservation> findUserReservationsByStatus(
        @Param("userId") Long userId, 
        @Param("status") String status
);
```

---

## 7. Service Layer Patterns

### Pattern 1: Pass-Through Service

Simple services that just delegate to repositories:

```java
@Override
public List<Station> getAllStations() {
    return stationRepository.findAll();
}
```

### Pattern 2: Business Logic Service

Services that add validation and business rules:

```java
@Override
public Route createRoute(CreateRouteRequest request) {
    // Validation
    if (origin.getId().equals(destination.getId())) {
        throw new RuntimeException("Cannot create circular route");
    }
    
    // Business logic
    Route route = new Route(origin, destination, capacity);
    return routeRepository.save(route);
}
```

### Pattern 3: Orchestration Service

Services that coordinate multiple repositories/services:

```java
@Override
public Reservation createReservation(CreateReservationRequest request) {
    // Coordinate multiple repositories
    User user = userService.findByUsername(username);
    Route route = routeRepository.findById(routeId);
    
    // Business logic
    Reservation reservation = new Reservation();
    reservation.setUser(user);
    reservation.setRoute(route);
    
    // Save
    return reservationRepository.save(reservation);
}
```

### Pattern 4: Security-Aware Service

Services that enforce access control:

```java
@Override
public Reservation cancelReservation(Long reservationId) {
    Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(...);
    
    // Security check
    if (!isAdmin && !reservation.getUser().getId().equals(currentUserId)) {
        throw new RuntimeException("Access denied");
    }
    
    reservation.setStatus("CANCELLED");
    return reservationRepository.save(reservation);
}
```

### Pattern 5: Inheritance-Aware Service

Services that work with JPA inheritance to create different entity types:

```java
@Service
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    
    @Override
    public User register(RegisterRequest request) {
        // Business logic: Create RegularUser (default type)
        RegularUser user = new RegularUser(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        );
        // Repository handles inheritance automatically
        return userRepository.save(user);
    }
    
    @Override
    public User createAdminUser(...) {
        // Business logic: Create AdminUser (different type)
        AdminUser adminUser = new AdminUser(
                username,
                email,
                passwordEncoder.encode(password)
        );
        // Same repository, different entity subclass
        return userRepository.save(adminUser);
    }
    
    @Override
    public User findByUsername(String username) {
        // Repository returns correct subclass based on user_type
        User user = userRepository.findByUsername(username)
                .orElseThrow(...);
        
        // user.getRole() returns "USER" or "ADMIN" based on subclass
        return user;
    }
}
```

**Key Points:**
- Service creates specific subclasses, not base class
- Repository defined for base class handles all subclasses
- JPA automatically manages discriminator column
- Queries return correct subclass instances

---

## 8. Benefits of Repository-Service Pattern

### 1. Separation of Concerns
- **Repository**: Data access only
- **Service**: Business logic only
- **Controller**: HTTP handling only

### 2. Testability
```java
// Easy to mock repositories in unit tests
@Test
void testCreateReservation() {
    ReservationRepository mockRepo = mock(ReservationRepository.class);
    ReservationService service = new ReservationServiceImpl(mockRepo, ...);
    // Test business logic without database
}
```

### 3. Reusability
- Services can be used by multiple controllers
- Repositories can be used by multiple services

### 4. Maintainability
- Changes to database structure only affect repositories
- Business logic changes only affect services

### 5. Transaction Management
- Spring automatically manages transactions at service level
- Ensures data consistency

### 6. Code Reduction
- No SQL writing (Spring Data JPA generates it)
- No boilerplate CRUD code
- Focus on business logic

---

## 9. Complete Flow Example

### Creating a Reservation - Full Stack Trace

```
1. HTTP POST /api/reservations
   ↓
2. ReservationController.createReservation(request)
   ↓
3. ReservationService.createReservation(request)
   ↓
   ├─→ UserService.findByUsername(username)
   │   └─→ UserRepository.findByUsername(username)
   │       └─→ SQL: SELECT * FROM users WHERE username = ?
   │       └─→ JPA maps user_type to RegularUser or AdminUser
   │       └─→ Returns User subclass instance
   │
   ├─→ RouteRepository.findById(routeId)
   │   └─→ SQL: SELECT * FROM routes WHERE id = ?
   │
   ├─→ Business Logic:
   │   - Set passenger defaults
   │   - Validate data
   │   - Create Reservation object
   │
   └─→ ReservationRepository.save(reservation)
       └─→ SQL: INSERT INTO reservations (...) VALUES (...)
       └─→ Database triggers fire
       └─→ Returns saved entity with ID
   ↓
4. ReservationRepository.findById(savedId) [refresh]
   └─→ SQL: SELECT * FROM reservations WHERE id = ?
   └─→ Returns entity with denormalized fields
   ↓
5. Return Reservation to Controller
   ↓
6. Serialize to JSON
   ↓
7. HTTP 201 Created Response
```

### Registering a User - Full Stack Trace (with Inheritance)

```
1. HTTP POST /api/auth/register
   ↓
2. AuthController.register(request)
   ↓
3. UserService.register(request)
   ↓
   ├─→ UserRepository.existsByUsername(username)
   │   └─→ SQL: SELECT COUNT(*) > 0 FROM users WHERE username = ?
   │
   ├─→ UserRepository.existsByEmail(email)
   │   └─→ SQL: SELECT COUNT(*) > 0 FROM users WHERE email = ?
   │
   ├─→ Business Logic:
   │   - Create RegularUser instance (subclass of User)
   │   - Set fields from request
   │   - Encode password
   │
   └─→ UserRepository.save(regularUser)
       └─→ JPA detects RegularUser subclass
       └─→ SQL: INSERT INTO users (..., user_type, role) VALUES (..., 'USER', 'USER')
       └─→ JPA automatically sets user_type discriminator
       └─→ Returns saved RegularUser instance
   ↓
4. Generate JWT token
   ↓
5. Return AuthResponse with token
   ↓
6. HTTP 201 Created Response
```

---

## 10. Best Practices

### Repository Best Practices:

1. **Keep Repositories Simple**: Only data access, no business logic
2. **Use Method Naming**: Let Spring generate queries automatically
3. **Custom Queries When Needed**: Use `@Query` for complex queries
4. **Return Appropriate Types**: `Optional<T>` for single results, `List<T>` for collections

### Service Best Practices:

1. **One Service Per Domain**: `ReservationService`, `RouteService`, etc.
2. **Business Logic Only**: No HTTP concerns, no database details
3. **Use Other Services**: Services can call other services
4. **Handle Exceptions**: Convert database exceptions to business exceptions
5. **Validate Input**: Check business rules before saving

### Dependency Injection Best Practices:

1. **Constructor Injection**: Preferred over field injection
2. **Final Fields**: Make repository fields `final` for immutability
3. **Interface-Based**: Depend on interfaces, not implementations

---

## Summary

The Repository-Service pattern provides:

- **Repositories**: Data access layer (Spring Data JPA)
  - Automatic CRUD operations
  - Query method generation
  - Custom queries when needed
  - **JPA Inheritance Support**: Repositories work seamlessly with inherited entities

- **Services**: Business logic layer
  - Orchestrates repositories
  - Enforces business rules
  - Manages transactions
  - Handles security
  - **Creates Entity Subclasses**: Services instantiate specific subclasses (e.g., `RegularUser`, `AdminUser`)

- **JPA Inheritance Integration**:
  - Repositories defined for base classes handle all subclasses
  - Services create specific subclasses based on business logic
  - JPA automatically manages discriminator columns
  - Queries return correct subclass instances

- **Benefits**: Clean architecture, testability, maintainability, and reduced boilerplate code

This pattern is the foundation of the application's data persistence and business logic implementation, including support for JPA inheritance strategies like the `SINGLE_TABLE` approach used for the `User` entity hierarchy.

