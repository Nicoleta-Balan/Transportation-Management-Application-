# Repository Services Implementation

## Overview

The Transportation Management System uses the **Repository-Service Pattern**, a layered architecture that separates data access (Repository) from business logic (Service). This pattern provides clean separation of concerns, testability, and maintainability.

```
HTTP Request → Controller → Service → Repository (Spring Data JPA) → JPA/Hibernate → PostgreSQL Database
```

**Key Principles**:
- **Repositories**: Data access only - no business logic
- **Services**: Business logic and orchestration - no direct database access
- **Spring Data JPA**: Automatic CRUD operations and query generation
- **Dependency Injection**: Services depend on repositories via constructor injection

---

## 1. Repository Layer (Data Access)

### 1.1 Repository Pattern

**Approach**: Spring Data JPA interfaces extending `JpaRepository<Entity, ID>`

**All Repositories** (13 total):
1. `FarePolicyHistoryRepository` - History records for fare policies
2. `FarePolicyRepository` - Fare policy management
3. `ReservationRepository` - Reservation management
4. `ReservationStatusHistoryRepository` - Reservation status change history
5. `RevenueSummaryRepository` - Revenue summary records
6. `RouteAvailabilityRepository` - Route availability records
7. `RouteRepository` - Route management
8. `RouteStatisticsRepository` - Route statistics records
9. `RouteTimetableEntryRepository` - Timetable entry management
10. `RouteTimetableRepository` - Route timetable management
11. `StationRepository` - Station management
12. `UserRepository` - User management (with JPA inheritance)
13. `VatRateRepository` - VAT rate management

### 1.2 Automatic CRUD Operations

All repositories automatically provide these methods from `JpaRepository`:

```java
// All these methods are available automatically:
repository.save(entity);           // INSERT or UPDATE
repository.findById(id);           // SELECT * WHERE id = ?
repository.findAll();              // SELECT * FROM table
repository.delete(entity);         // DELETE WHERE id = ?
repository.count();                // SELECT COUNT(*) FROM table
repository.existsById(id);         // SELECT COUNT(*) WHERE id = ? > 0
repository.deleteById(id);         // DELETE WHERE id = ?
repository.saveAll(entities);     // Batch INSERT/UPDATE
repository.deleteAll();            // DELETE FROM table
```

### 1.3 Repository Implementations

#### ReservationRepository

**Location**: `src/main/java/multitier/trans/repository/ReservationRepository.java`

**Custom Methods**:
```java
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    // Method name derivation - Spring generates SQL automatically
    List<Reservation> findByRouteId(Long routeId);
    // → SQL: SELECT * FROM reservations WHERE route_id = ?
    
    List<Reservation> findByUserId(Long userId);
    // → SQL: SELECT * FROM reservations WHERE user_id = ?
    
    List<Reservation> findByPassengerName(String passengerName);
    // → SQL: SELECT * FROM reservations WHERE passenger_name = ?
    
    // Deprecated: Use FareCalculationService instead
    @Deprecated
    @Query(value = "SELECT * FROM calculate_reservation_fare(...)", nativeQuery = true)
    Map<String, Object> calculateFare(...);
}
```

**Usage**: Used by `ReservationServiceImpl` for reservation queries and persistence.

#### FarePolicyRepository

**Location**: `src/main/java/multitier/trans/repository/FarePolicyRepository.java`

**Custom Methods**:
```java
@Repository
public interface FarePolicyRepository extends JpaRepository<FarePolicy, Long> {
    
    // Method name derivation
    Optional<FarePolicy> findByRouteIdAndPassengerCategoryAndVehicleClass(
            Long routeId,
            PassengerCategory passengerCategory,
            VehicleClass vehicleClass
    );
    // → SQL: SELECT * FROM fare_policies 
    //        WHERE route_id = ? AND passenger_category = ? AND vehicle_class = ?
    
    // Custom JPQL query - finds active fare policy for a date
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
    
    // Custom JPQL query - checks for overlapping active policies
    @Query("SELECT COUNT(f) > 0 FROM FarePolicy f WHERE f.route.id = :routeId " +
           "AND f.passengerCategory = :category " +
           "AND f.vehicleClass = :vehicleClass " +
           "AND f.status = :status " +
           "AND f.id != :excludeId " +
           "AND f.effectiveFrom < :maxEffectiveTo " +
           "AND (f.effectiveTo IS NULL OR f.effectiveTo > :effectiveFrom)")
    boolean existsOverlappingActivePolicy(...);
}
```

**Key Features**:
- Uses JPQL (Java Persistence Query Language) for type-safe queries
- Supports enum parameters (`PolicyStatus`, `PassengerCategory`, `VehicleClass`)
- Default method with convenience overload

**Usage**: Used by `FarePolicyServiceImpl` for fare policy lookups and validation.

#### VatRateRepository

**Location**: `src/main/java/multitier/trans/repository/VatRateRepository.java`

**Custom Methods**:
```java
@Repository
public interface VatRateRepository extends JpaRepository<VatRate, Long> {
    
    // Custom JPQL query - finds active VAT rate for a date
    @Query("SELECT v FROM VatRate v WHERE v.effectiveFrom <= :date " +
           "AND (v.effectiveTo IS NULL OR v.effectiveTo > :date) " +
           "ORDER BY v.effectiveFrom DESC")
    Optional<VatRate> findActiveVatRateForDate(@Param("date") LocalDateTime date);
    
    // Default method - convenience wrapper
    default Optional<VatRate> findCurrentVatRate() {
        return findActiveVatRateForDate(LocalDateTime.now());
    }
    
    // Custom JPQL query - checks for overlapping VAT rates
    @Query("SELECT COUNT(v) > 0 FROM VatRate v WHERE v.id != :excludeId " +
           "AND v.effectiveFrom < :maxEffectiveTo " +
           "AND (v.effectiveTo IS NULL OR v.effectiveTo > :effectiveFrom)")
    boolean existsOverlappingVatRate(...);
}
```

**Key Features**:
- Default methods for convenience
- Date-based queries for effective rates
- Overlap detection for validation

**Usage**: Used by `VatRateServiceImpl` and `FareCalculationServiceImpl` for VAT rate lookups.

#### UserRepository

**Location**: `src/main/java/multitier/trans/repository/UserRepository.java`

**Custom Methods**:
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Method name derivation
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

**Key Features**:
- Works with JPA inheritance (`SINGLE_TABLE` strategy)
- Queries return correct subclass (`RegularUser` or `AdminUser`) based on `user_type` discriminator
- All user types stored in single `users` table

**Usage**: Used by `UserServiceImpl` for user authentication and registration.

#### StationRepository

**Location**: `src/main/java/multitier/trans/repository/StationRepository.java`

**Custom Methods**:
```java
@Repository
public interface StationRepository extends JpaRepository<Station, Long> {
    
    // Method name derivation
    Station findByName(String name);
    // → SQL: SELECT * FROM stations WHERE name = ?
}
```

**Usage**: Used by `StationServiceImpl` for station lookups.

#### RouteRepository

**Location**: `src/main/java/multitier/trans/repository/RouteRepository.java`

**Custom Methods**: None (uses only automatic CRUD methods)

**Usage**: Used by `RouteServiceImpl`, `ReservationServiceImpl`, and other services for route management.

#### RouteTimetableRepository

**Location**: `src/main/java/multitier/trans/repository/RouteTimetableRepository.java`

**Custom Methods**:
```java
public interface RouteTimetableRepository extends JpaRepository<RouteTimetable, Long> {
    
    // EntityGraph for eager loading of entries (component entities)
    @EntityGraph(attributePaths = "entries")
    List<RouteTimetable> findByRouteId(Long routeId);
    
    @EntityGraph(attributePaths = "entries")
    Optional<RouteTimetable> findByIdAndRouteId(Long id, Long routeId);
}
```

**Key Features**:
- Uses `@EntityGraph` to eagerly load `RouteTimetableEntry` collection
- Prevents N+1 query problem when accessing timetable entries
- Loads parent and children in single query

**Usage**: Used by `TimetableServiceImpl` for timetable management with entries.

#### History Repositories

**FarePolicyHistoryRepository**:
- Location: `src/main/java/multitier/trans/repository/FarePolicyHistoryRepository.java`
- Purpose: Stores history of fare policy changes
- Used by: `FarePolicyHistoryListener` (Entity Listener)

**ReservationStatusHistoryRepository**:
- Location: `src/main/java/multitier/trans/repository/ReservationStatusHistoryRepository.java`
- Purpose: Stores history of reservation status changes
- Used by: `ReservationServiceImpl.recordStatusHistory()`

#### Other Repositories

**RouteAvailabilityRepository**, **RouteStatisticsRepository**, **RevenueSummaryRepository**, **RouteTimetableEntryRepository**:
- Provide standard CRUD operations
- Used by various services for data access

---

## 2. Service Layer (Business Logic)

### 2.1 Service Pattern

**All Services** (11 total):
1. `AnalyticsService` / `AnalyticsServiceImpl` - Analytics and reporting
2. `FareCalculationService` / `FareCalculationServiceImpl` - Fare calculation logic
3. `FarePolicyService` / `FarePolicyServiceImpl` - Fare policy management
4. `FinancialService` / `FinancialServiceImpl` - Financial summaries and revenue
5. `ReservationService` / `ReservationServiceImpl` - Reservation management
6. `RouteService` / `RouteServiceImpl` - Route management
7. `SeatAvailabilityService` / `SeatAvailabilityServiceImpl` - Seat availability calculation
8. `StationService` / `StationServiceImpl` - Station management
9. `TimetableService` / `TimetableServiceImpl` - Route timetable management
10. `UserService` / `UserServiceImpl` - User management and authentication
11. `VatRateService` / `VatRateServiceImpl` - VAT rate management

### 2.2 Service Responsibilities

**Key Responsibilities**:
1. **Business Logic**: Validations, calculations, workflows
2. **Orchestration**: Coordinates multiple repositories
3. **Transaction Management**: Ensures data consistency (`@Transactional`)
4. **Security**: Access control and authorization
5. **Error Handling**: Converts exceptions to meaningful errors
6. **Validation**: Business rule enforcement (moved from database triggers)

### 2.3 Service Implementations

#### ReservationServiceImpl

**Location**: `src/main/java/multitier/trans/service/ReservationServiceImpl.java`

**Dependencies**:
- `ReservationRepository` - Data access
- `RouteRepository` - Route lookups
- `UserService` - User authentication
- `ReservationFactory` - Entity creation
- `FareCalculationService` - Fare calculation
- `SeatAvailabilityService` - Seat availability checks
- `ReservationStatusHistoryRepository` - Status history tracking

**Key Methods**:
```java
@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {
    
    @Override
    public Reservation createReservation(CreateReservationRequest request) {
        // 1. Get authenticated user
        User user = userService.findByUsername(username);
        
        // 2. Load route
        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new RuntimeException("Route not found"));
        
        // 3. Business validation: Check seat availability
        if (!seatAvailabilityService.checkSeatAvailability(...)) {
            throw new RuntimeException("Insufficient seats available");
        }
        
        // 4. Business validation: Validate time constraints
        if (request.getArrivalTime().isBefore(request.getDepartureTime())) {
            throw new RuntimeException("Arrival time must be after departure time");
        }
        
        // 5. Create reservation using factory
        Reservation reservation = reservationFactory.createReservation(...);
        
        // 6. Update denormalized fields (service layer)
        updateDenormalizedFields(reservation, route);
        
        // 7. Save reservation
        Reservation saved = reservationRepository.save(reservation);
        
        // 8. Record status history
        recordStatusHistory(saved, null, ReservationStatus.CONFIRMED, "Reservation created");
        
        return saved;
    }
    
    @Override
    public Reservation cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        
        ReservationStatus oldStatus = reservation.getStatus();
        reservation.setStatus(ReservationStatus.CANCELLED);
        
        Reservation saved = reservationRepository.save(reservation);
        
        // Record status change history
        recordStatusHistory(saved, oldStatus, ReservationStatus.CANCELLED, "Reservation cancelled");
        
        return saved;
    }
    
    private void updateDenormalizedFields(Reservation reservation, Route route) {
        // Set station names from route
        reservation.setOriginStationName(route.getOriginStation().getName());
        reservation.setDestinationStationName(route.getDestinationStation().getName());
        
        // Calculate fare using FareCalculationService
        FareCalculationResponse fare = fareCalculationService.calculateFare(...);
        reservation.setBaseFare(fare.getBaseFare());
        reservation.setVatAmount(fare.getVatAmount());
        reservation.setTotalFare(fare.getTotalFare());
    }
}
```

**Key Features**:
- Uses factory pattern for entity creation
- Service layer validation (replaces database triggers)
- Service layer denormalization (replaces database triggers)
- Manual status history tracking (service has access to old/new values)
- Coordinates multiple services and repositories

#### FarePolicyServiceImpl

**Location**: `src/main/java/multitier/trans/service/FarePolicyServiceImpl.java`

**Dependencies**:
- `FarePolicyRepository` - Data access

**Key Methods**:
```java
@Service
@Transactional
public class FarePolicyServiceImpl implements FarePolicyService {
    
    @Override
    @Transactional(readOnly = true)
    public FarePolicy getActiveFarePolicy(Long routeId, PassengerCategory category, 
                                         VehicleClass vehicleClass, LocalDateTime date) {
        return farePolicyRepository.findActiveFarePolicy(
                routeId, category, vehicleClass, date.toLocalDate())
                .orElseThrow(() -> new RuntimeException("No active fare policy found"));
    }
    
    @Override
    public void validateFarePolicy(FarePolicy farePolicy) {
        // Business validation: Date range check
        if (farePolicy.getEffectiveTo() != null && 
            !farePolicy.getEffectiveTo().isAfter(farePolicy.getEffectiveFrom())) {
            throw new RuntimeException("Effective end date must be after effective start date");
        }
        
        // Business validation: Check for overlapping active policies
        if (PolicyStatus.ACTIVE.equals(farePolicy.getStatus())) {
            boolean hasOverlap = farePolicyRepository.existsOverlappingActivePolicy(...);
            if (hasOverlap) {
                throw new RuntimeException("Overlapping active fare policy exists");
            }
        }
    }
    
    @Override
    public FarePolicy saveFarePolicy(FarePolicy farePolicy) {
        validateFarePolicy(farePolicy);  // Business validation before save
        return farePolicyRepository.save(farePolicy);
    }
}
```

**Key Features**:
- Business validation in service layer (replaces database triggers)
- Uses custom repository queries for complex lookups
- Read-only transactions for queries (`@Transactional(readOnly = true)`)

#### FareCalculationServiceImpl

**Location**: `src/main/java/multitier/trans/service/FareCalculationServiceImpl.java`

**Dependencies**:
- `FarePolicyService` - Get base price
- `VatRateService` - Get VAT rate

**Key Methods**:
```java
@Service
@Transactional(readOnly = true)
public class FareCalculationServiceImpl implements FareCalculationService {
    
    @Override
    public FareCalculationResponse calculateFare(Long routeId, PassengerCategory passengerCategory,
                                                 VehicleClass vehicleClass, Integer seatCount,
                                                 LocalDateTime departureTime) {
        // 1. Get base price per seat from fare policy
        BigDecimal basePricePerSeat = farePolicyService.getActiveFarePolicyPrice(
                routeId, passengerCategory, vehicleClass, departureTime);
        
        // 2. Calculate base fare (price per seat * number of seats)
        BigDecimal baseFare = basePricePerSeat
                .multiply(BigDecimal.valueOf(seatCount))
                .setScale(2, RoundingMode.HALF_UP);
        
        // 3. Get VAT rate for the departure date
        BigDecimal vatRate = vatRateService.getVatRateForDate(departureTime);
        
        // 4. Calculate VAT amount
        BigDecimal vatAmount = baseFare
                .multiply(vatRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        // 5. Calculate total fare (base + VAT)
        BigDecimal totalFare = baseFare.add(vatAmount).setScale(2, RoundingMode.HALF_UP);
        
        return new FareCalculationResponse(baseFare, vatAmount, totalFare, vatRate);
    }
}
```

**Key Features**:
- Replaces database function `calculate_reservation_fare()`
- Orchestrates multiple services
- Type-safe calculations using Java BigDecimal
- Read-only service (no data modification)

#### UserServiceImpl

**Location**: `src/main/java/multitier/trans/service/UserServiceImpl.java`

**Dependencies**:
- `UserRepository` - Data access
- `UserFactory` - Entity creation

**Key Methods**:
```java
@Service
public class UserServiceImpl implements UserService {
    
    @Override
    @Transactional
    public User register(RegisterRequest request) {
        // Business validation: Check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        // Business validation: Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Use Factory to create RegularUser (JPA inheritance)
        User user = userFactory.createRegularUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                request.getPhone()
        );
        
        return userRepository.save(user);
    }
    
    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
    
    @Override
    public User createAdminUser(String username, String email, String password, ...) {
        // Business validation
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        
        // Use Factory to create AdminUser (JPA inheritance)
        User adminUser = userFactory.createAdminUser(...);
        
        return userRepository.save(adminUser);
    }
}
```

**Key Features**:
- Works with JPA inheritance (`RegularUser`, `AdminUser`)
- Uses factory pattern for entity creation
- Business validation before persistence
- Repository automatically handles discriminator column

#### StationServiceImpl

**Location**: `src/main/java/multitier/trans/service/StationServiceImpl.java`

**Dependencies**:
- `StationRepository` - Data access
- `RouteRepository` - Route lookups
- `ReservationRepository` - Reservation updates

**Key Methods**:
```java
@Service
@Transactional
public class StationServiceImpl implements StationService {
    
    @Override
    public Station updateStation(Station station) {
        Station existingStation = stationRepository.findById(station.getId())
                .orElseThrow(() -> new RuntimeException("Station not found"));
        
        // Business validation: Prevent closing station if it has routes
        if (StationStatus.CLOSED.equals(station.getStatus()) && 
            !StationStatus.CLOSED.equals(existingStation.getStatus())) {
            List<Route> routes = routeRepository.findAll().stream()
                    .filter(route -> route.getOriginStation().getId().equals(station.getId()) || 
                                    route.getDestinationStation().getId().equals(station.getId()))
                    .toList();
            
            if (!routes.isEmpty()) {
                throw new RuntimeException("Cannot close station because it has routes");
            }
        }
        
        // Update denormalized fields in reservations if station name changed
        String oldName = existingStation.getName();
        String newName = station.getName();
        if (!oldName.equals(newName)) {
            List<Reservation> reservations = reservationRepository.findByRouteId(...);
            for (Reservation reservation : reservations) {
                if (reservation.getOriginStationName().equals(oldName)) {
                    reservation.setOriginStationName(newName);
                }
                if (reservation.getDestinationStationName().equals(oldName)) {
                    reservation.setDestinationStationName(newName);
                }
            }
            reservationRepository.saveAll(reservations);
        }
        
        return stationRepository.save(station);
    }
}
```

**Key Features**:
- Business validation (replaces database triggers)
- Service layer denormalization (updates related entities)
- Coordinates multiple repositories

#### VatRateServiceImpl

**Location**: `src/main/java/multitier/trans/service/VatRateServiceImpl.java`

**Dependencies**:
- `VatRateRepository` - Data access

**Key Features**:
- Business validation for overlapping VAT rates
- Date-based lookups for effective rates
- Similar pattern to `FarePolicyServiceImpl`

#### Other Services

**RouteServiceImpl**, **TimetableServiceImpl**, **SeatAvailabilityServiceImpl**, **FinancialServiceImpl**, **AnalyticsServiceImpl**:
- Follow similar patterns
- Use repositories for data access
- Implement business logic and validation
- Coordinate multiple repositories when needed

---

## 3. Repository-Service Interaction Patterns

### 3.1 Pattern 1: Simple Pass-Through

**Service delegates directly to repository**:

```java
@Service
public class StationServiceImpl implements StationService {
    
    @Override
    public List<Station> getAllStations() {
        return stationRepository.findAll();  // Direct delegation
    }
}
```

**Flow**: `Controller → Service → Repository → Database`

### 3.2 Pattern 2: Business Logic + Validation

**Service adds validation before repository call**:

```java
@Service
public class UserServiceImpl implements UserService {
    
    @Override
    public User register(RegisterRequest request) {
        // Business validation
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        // Business logic: Create entity
        User user = userFactory.createRegularUser(...);
        
        // Repository call
        return userRepository.save(user);
    }
}
```

**Flow**: `Controller → Service (validation) → Repository → Database`

### 3.3 Pattern 3: Orchestration (Multiple Repositories)

**Service coordinates multiple repositories**:

```java
@Service
public class ReservationServiceImpl implements ReservationService {
    
    @Override
    public Reservation createReservation(CreateReservationRequest request) {
        // Multiple repository calls
        User user = userService.findByUsername(username);  // Indirect repository call
        Route route = routeRepository.findById(request.getRouteId());
        
        // Business logic
        Reservation reservation = reservationFactory.createReservation(...);
        
        // Repository call
        return reservationRepository.save(reservation);
    }
}
```

**Flow**: `Controller → Service → Multiple Repositories → Database`

### 3.4 Pattern 4: Service-to-Service Communication

**Service uses other services**:

```java
@Service
public class ReservationServiceImpl implements ReservationService {
    
    private final FareCalculationService fareCalculationService;
    
    private void updateDenormalizedFields(Reservation reservation, Route route) {
        // Use another service for calculation
        FareCalculationResponse fare = fareCalculationService.calculateFare(...);
        reservation.setBaseFare(fare.getBaseFare());
        reservation.setVatAmount(fare.getVatAmount());
        reservation.setTotalFare(fare.getTotalFare());
    }
}
```

**Flow**: `Service A → Service B → Repository → Database`

### 3.5 Pattern 5: Custom Query Usage

**Service uses custom repository queries**:

```java
@Service
public class FarePolicyServiceImpl implements FarePolicyService {
    
    @Override
    public FarePolicy getActiveFarePolicy(...) {
        // Uses custom JPQL query from repository
        return farePolicyRepository.findActiveFarePolicy(...)
                .orElseThrow(() -> new RuntimeException("Not found"));
    }
    
    @Override
    public void validateFarePolicy(FarePolicy farePolicy) {
        // Uses custom query for validation
        boolean hasOverlap = farePolicyRepository.existsOverlappingActivePolicy(...);
        if (hasOverlap) {
            throw new RuntimeException("Overlapping policy exists");
        }
    }
}
```

**Flow**: `Service → Repository (custom query) → Database`

---

## 4. Dependency Injection

### 4.1 Constructor Injection Pattern

**All services use constructor injection**:

```java
@Service
public class ReservationServiceImpl implements ReservationService {
    
    // Final fields (immutable)
    private final ReservationRepository reservationRepository;
    private final RouteRepository routeRepository;
    private final UserService userService;
    
    // Constructor injection (recommended)
    @Autowired
    public ReservationServiceImpl(
            ReservationRepository reservationRepository,
            RouteRepository routeRepository,
            UserService userService) {
        this.reservationRepository = reservationRepository;
        this.routeRepository = routeRepository;
        this.userService = userService;
    }
}
```

**Benefits**:
- ✅ Immutability: Fields can be `final`
- ✅ Testability: Easy to mock in unit tests
- ✅ Required Dependencies: Compiler ensures all dependencies are provided
- ✅ No Reflection: Better performance than field injection

---

## 5. Transaction Management

### 5.1 Automatic Transactions

**All services use `@Transactional`**:

```java
@Service
@Transactional  // All methods run in a transaction
public class ReservationServiceImpl implements ReservationService {
    
    @Override
    public Reservation createReservation(...) {
        // This entire method runs in a transaction
        // If any step fails, everything rolls back
        
        Route route = routeRepository.findById(...);
        Reservation reservation = reservationFactory.createReservation(...);
        return reservationRepository.save(reservation);
    }
}
```

**Transaction Behavior**:
- **All or Nothing**: If any operation fails, the entire transaction rolls back
- **Automatic**: No need to manually start/commit transactions
- **Isolation**: Each transaction sees a consistent view of data

### 5.2 Read-Only Transactions

**Services that only read data use `@Transactional(readOnly = true)`**:

```java
@Service
@Transactional(readOnly = true)  // Optimized for read operations
public class FareCalculationServiceImpl implements FareCalculationService {
    
    @Override
    public FareCalculationResponse calculateFare(...) {
        // Read-only operations only
        BigDecimal price = farePolicyService.getActiveFarePolicyPrice(...);
        BigDecimal vatRate = vatRateService.getVatRateForDate(...);
        // ... calculations (no database writes)
    }
}
```

**Benefits**:
- Performance optimization (database can optimize for read-only)
- Clear intent (service doesn't modify data)

---

## 6. Query Method Patterns

### 6.1 Method Name Derivation

**Spring Data JPA generates SQL from method names**:

```java
// Repository
List<Reservation> findByRouteId(Long routeId);
// → SQL: SELECT * FROM reservations WHERE route_id = ?

Optional<User> findByUsername(String username);
// → SQL: SELECT * FROM users WHERE username = ?

boolean existsByEmail(String email);
// → SQL: SELECT COUNT(*) > 0 FROM users WHERE email = ?
```

**Naming Convention**:
- `findBy` + PropertyName → `WHERE property = ?`
- `findBy` + Property1 + `And` + Property2 → `WHERE property1 = ? AND property2 = ?`
- `existsBy` + PropertyName → `SELECT COUNT(*) > 0 WHERE property = ?`

### 6.2 Custom JPQL Queries

**Type-safe queries using entity names and properties**:

```java
@Query("SELECT f FROM FarePolicy f WHERE f.route.id = :routeId " +
       "AND f.passengerCategory = :category " +
       "AND f.status = :status " +
       "AND f.effectiveFrom <= :date " +
       "AND (f.effectiveTo IS NULL OR f.effectiveTo > :date)")
Optional<FarePolicy> findActiveFarePolicy(
        @Param("routeId") Long routeId,
        @Param("category") PassengerCategory category,
        @Param("status") PolicyStatus status,
        @Param("date") LocalDate date
);
```

**Benefits**:
- Type-safe (uses entity names, not table names)
- Compile-time validation
- Database-agnostic (works with any JPA-compliant database)

### 6.3 Entity Graph (Eager Loading)

**Prevents N+1 query problem**:

```java
@EntityGraph(attributePaths = "entries")
List<RouteTimetable> findByRouteId(Long routeId);
// → Loads RouteTimetable and RouteTimetableEntry in single query
```

**Usage**: When you need to access child entities, use `@EntityGraph` to load them eagerly.

---

## 7. Complete Flow Examples

### 7.1 Creating a Reservation - Full Stack Trace

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
   ├─→ UserService.findByUsername(username)
   │   └─→ UserRepository.findByUsername(username)
   │       └─→ SQL: SELECT * FROM users WHERE username = ?
   │       └─→ JPA maps user_type to RegularUser or AdminUser
   │       └─→ Returns User subclass instance
   │
   ├─→ RouteRepository.findById(routeId)
   │   └─→ SQL: SELECT * FROM routes WHERE id = ?
   │
   ├─→ SeatAvailabilityService.checkSeatAvailability(...)
   │   └─→ ReservationRepository.findByRouteId(...)
   │       └─→ SQL: SELECT * FROM reservations WHERE route_id = ?
   │   └─→ Business logic: Calculate available seats
   │
   ├─→ Business Logic:
   │   - Validate time constraints
   │   - Create Reservation using ReservationFactory
   │
   ├─→ FareCalculationService.calculateFare(...)
   │   ├─→ FarePolicyService.getActiveFarePolicyPrice(...)
   │   │   └─→ FarePolicyRepository.findActiveFarePolicy(...)
   │   │       └─→ SQL: SELECT * FROM fare_policies WHERE ...
   │   └─→ VatRateService.getVatRateForDate(...)
   │       └─→ VatRateRepository.findActiveVatRateForDate(...)
   │           └─→ SQL: SELECT * FROM vat_rates WHERE ...
   │   └─→ Business logic: Calculate base fare, VAT, total
   │
   ├─→ updateDenormalizedFields(reservation, route)
   │   - Sets originStationName, destinationStationName
   │   - Sets baseFare, vatAmount, totalFare
   │
   └─→ ReservationRepository.save(reservation)
       └─→ SQL: INSERT INTO reservations (...) VALUES (...)
       └─→ BaseEntityListener.prePersist() sets createdAt, updatedAt
       └─→ Returns saved entity with ID
   ↓
4. recordStatusHistory(saved, null, ReservationStatus.CONFIRMED, "Reservation created")
   └─→ ReservationStatusHistoryRepository.save(history)
       └─→ SQL: INSERT INTO reservation_status_history (...) VALUES (...)
   ↓
5. Return Reservation object to controller
   ↓
6. Serialize to JSON
   ↓
7. HTTP 201 Created Response
```

### 7.2 Registering a User - Full Stack Trace (with JPA Inheritance)

```
1. HTTP POST /api/auth/register
   Request Body: {
     "username": "john",
     "email": "john@example.com",
     "password": "password123",
     "firstName": "John",
     "lastName": "Doe"
   }
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
   │   - UserFactory.createRegularUser(...)
   │   - Creates RegularUser instance (subclass of User)
   │   - Encodes password
   │   - Sets fields from request
   │
   └─→ UserRepository.save(regularUser)
       └─→ JPA detects RegularUser subclass
       └─→ SQL: INSERT INTO users (..., user_type, role) VALUES (..., 'USER', 'USER')
       └─→ JPA automatically sets user_type discriminator = 'USER'
       └─→ BaseEntityListener.prePersist() sets createdAt, updatedAt
       └─→ Returns saved RegularUser instance
   ↓
4. Generate JWT token
   ↓
5. Return AuthResponse with token
   ↓
6. HTTP 201 Created Response
```

### 7.3 Creating a Fare Policy - Full Stack Trace

```
1. HTTP POST /api/fare-policies
   Request Body: {
     "routeId": 1,
     "passengerCategory": "ADULT",
     "vehicleClass": "STANDARD",
     "price": 50.00,
     "effectiveFrom": "2025-01-01",
     "effectiveTo": null,
     "status": "ACTIVE"
   }
   ↓
2. FarePolicyController.createFarePolicy(request)
   ↓
3. FarePolicyService.saveFarePolicy(farePolicy)
   ↓
   ├─→ FarePolicyService.validateFarePolicy(farePolicy)
   │   ├─→ Business validation: Check date range
   │   │   if (effectiveTo != null && !effectiveTo.isAfter(effectiveFrom)) {
   │   │       throw new RuntimeException("Invalid date range");
   │   │   }
   │   │
   │   └─→ Business validation: Check for overlapping policies
   │       └─→ FarePolicyRepository.existsOverlappingActivePolicy(...)
   │           └─→ SQL: SELECT COUNT(*) > 0 FROM fare_policies WHERE ...
   │           └─→ Returns true if overlap exists
   │       if (hasOverlap) {
   │           throw new RuntimeException("Overlapping policy exists");
   │       }
   │
   └─→ FarePolicyRepository.save(farePolicy)
       └─→ SQL: INSERT INTO fare_policies (...) VALUES (...)
       └─→ BaseEntityListener.prePersist() sets createdAt, updatedAt
       └─→ FarePolicyHistoryListener.onFarePolicyCreated(...)
           └─→ FarePolicyHistoryRepository.save(history)
               └─→ SQL: INSERT INTO fare_policy_history (...) VALUES (...)
       └─→ Returns saved FarePolicy entity
   ↓
4. Return FarePolicy object to controller
   ↓
5. Serialize to JSON
   ↓
6. HTTP 201 Created Response
```

---

## 8. Key Patterns and Best Practices

### 8.1 Repository Best Practices

1. **Keep Repositories Simple**: Only data access, no business logic
2. **Use Method Naming**: Let Spring generate queries automatically
3. **Custom Queries When Needed**: Use `@Query` for complex queries
4. **Return Appropriate Types**: `Optional<T>` for single results, `List<T>` for collections
5. **Use Entity Graph**: Prevent N+1 queries when loading relationships

### 8.2 Service Best Practices

1. **One Service Per Domain**: `ReservationService`, `RouteService`, etc.
2. **Business Logic Only**: No HTTP concerns, no database details
3. **Use Other Services**: Services can call other services
4. **Handle Exceptions**: Convert database exceptions to business exceptions
5. **Validate Input**: Check business rules before saving
6. **Use Factories**: Encapsulate entity creation logic
7. **Service Layer Validation**: All validation moved from database to service layer

### 8.3 Dependency Injection Best Practices

1. **Constructor Injection**: Preferred over field injection
2. **Final Fields**: Make repository fields `final` for immutability
3. **Interface-Based**: Depend on interfaces, not implementations

### 8.4 Transaction Management Best Practices

1. **Service-Level Transactions**: Use `@Transactional` on service classes
2. **Read-Only Transactions**: Use `@Transactional(readOnly = true)` for query-only services
3. **Transaction Boundaries**: One transaction per service method

---

## 9. Summary

The Repository-Service implementation provides:

### Repositories (13 total)
- **Spring Data JPA**: Automatic CRUD operations
- **Query Method Generation**: SQL generated from method names
- **Custom Queries**: JPQL for complex queries
- **JPA Inheritance Support**: Repositories work seamlessly with inherited entities
- **Entity Graph**: Eager loading to prevent N+1 queries

### Services (11 total)
- **Business Logic**: Validation, calculations, workflows
- **Orchestration**: Coordinates multiple repositories
- **Transaction Management**: Ensures data consistency
- **Service-to-Service Communication**: Services can use other services
- **Factory Pattern**: Encapsulates entity creation
- **Service Layer Validation**: All validation moved from database triggers

### Key Features
- ✅ **Separation of Concerns**: Repository (data access) vs Service (business logic)
- ✅ **Testability**: Easy to mock repositories in unit tests
- ✅ **Type Safety**: JPQL queries use entity names, not table names
- ✅ **Automatic CRUD**: No boilerplate code for standard operations
- ✅ **Transaction Management**: Automatic transaction boundaries
- ✅ **JPA Inheritance**: Seamless support for entity inheritance
- ✅ **No Database Triggers**: All business logic in service layer

This architecture provides a clean, maintainable, and testable foundation for the application's data persistence and business logic implementation.

