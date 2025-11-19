package multitier.trans.config;

import multitier.trans.model.*;
import multitier.trans.model.enums.PassengerCategory;
import multitier.trans.model.enums.VehicleClass;
import multitier.trans.repository.*;
import multitier.trans.service.FarePolicyService;
import multitier.trans.service.VatRateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Data Initializer Configuration
 * 
 * This class initializes sample data using @PostConstruct after the Spring context is fully loaded.
 * This is the PRIMARY method for sample data initialization (replaces Flyway V4 migration).
 * 
 * Features:
 * - Only inserts data if tables are empty (idempotent)
 * - Type-safe Java implementation using JPA entities
 * - Can be enabled/disabled via application.properties
 * - Runs after Flyway migrations complete (V1-V3 for functions/triggers)
 * 
 * Configuration:
 * - Enable: Set app.data.initialize=true in application.properties (default: true)
 * - Disable: Set app.data.initialize=false
 */
@Configuration
@ConditionalOnProperty(name = "app.data.initialize", havingValue = "true", matchIfMissing = true)
@Order(100) // Run after Flyway migrations (which typically run at order 0-50)
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private RouteTimetableRepository timetableRepository;

    @Autowired
    private RouteTimetableEntryRepository timetableEntryRepository;

    @Autowired
    private FarePolicyRepository farePolicyRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VatRateRepository vatRateRepository;

    @Autowired
    private VatRateService vatRateService;

    @Autowired
    private FarePolicyService farePolicyService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void initializeData() {
        logger.info("Starting data initialization...");
        
        try {
            initializeVatRates();
            initializeStations();
            initializeRoutes();
            initializeTimetables();
            initializeFarePolicies();
            initializeReservations();
            
            logger.info("Data initialization completed successfully");
        } catch (Exception e) {
            logger.error("Error during data initialization", e);
            throw new RuntimeException("Failed to initialize sample data", e);
        }
    }

    private void initializeVatRates() {
        if (vatRateRepository.count() > 0) {
            logger.info("VAT rates table already contains data - skipping VAT rate initialization");
            return;
        }

        logger.info("Inserting default VAT rate...");
        
        // Create default VAT rate (19.00% effective from 2020-01-01)
        VatRate defaultVatRate = new VatRate();
        defaultVatRate.setRatePercentage(new BigDecimal("19.00"));
        defaultVatRate.setEffectiveFrom(LocalDateTime.of(2020, 1, 1, 0, 0));
        defaultVatRate.setDescription("Default VAT rate");
        defaultVatRate.setCreatedBy("SYSTEM");
        defaultVatRate.setCreatedAt(LocalDateTime.now());
        
        vatRateService.saveVatRate(defaultVatRate);
        logger.info("Inserted default VAT rate: 19.00%");
    }

    private void initializeStations() {
        if (stationRepository.count() > 0) {
            logger.info("Stations table already contains data - skipping sample station insertion");
            return;
        }

        logger.info("Inserting sample stations...");
        
        List<Station> stations = List.of(
            new Station("Central Station", "Main transportation hub in the city center", multitier.trans.model.enums.StationStatus.ACTIVE),
            new Station("North Terminal", "Northern terminal serving suburban areas", multitier.trans.model.enums.StationStatus.ACTIVE),
            new Station("South Station", "Southern terminal serving industrial areas", multitier.trans.model.enums.StationStatus.ACTIVE),
            new Station("East Depot", "Eastern depot serving residential areas", multitier.trans.model.enums.StationStatus.ACTIVE),
            new Station("West Hub", "Western hub serving commercial districts", multitier.trans.model.enums.StationStatus.ACTIVE),
            new Station("Airport Terminal", "Terminal serving the international airport", multitier.trans.model.enums.StationStatus.ACTIVE)
        );

        stationRepository.saveAll(stations);
        logger.info("Inserted {} sample stations", stations.size());
    }

    private void initializeRoutes() {
        if (routeRepository.count() > 0) {
            logger.info("Routes table already contains data - skipping sample route insertion");
            return;
        }

        logger.info("Inserting sample routes...");

        Station central = stationRepository.findByName("Central Station");
        Station north = stationRepository.findByName("North Terminal");
        Station south = stationRepository.findByName("South Station");
        Station east = stationRepository.findByName("East Depot");
        Station west = stationRepository.findByName("West Hub");
        Station airport = stationRepository.findByName("Airport Terminal");

        if (central == null || north == null || south == null || 
            east == null || west == null || airport == null) {
            logger.warn("Required stations not found - skipping route initialization");
            return;
        }

        List<Route> routes = List.of(
            new Route(central, north, 50),
            new Route(central, south, 50),
            new Route(central, east, 40),
            new Route(central, west, 50),
            new Route(central, airport, 60),
            new Route(north, south, 50),
            new Route(north, airport, 60),
            new Route(east, west, 40),
            new Route(south, airport, 60)
        );

        routeRepository.saveAll(routes);
        logger.info("Inserted {} sample routes", routes.size());
    }

    private void initializeTimetables() {
        if (timetableRepository.count() > 0) {
            logger.info("Route timetables table already contains data - skipping sample timetable insertion");
            return;
        }

        logger.info("Inserting sample route timetables...");

        Station central = stationRepository.findByName("Central Station");
        Station north = stationRepository.findByName("North Terminal");
        Station airport = stationRepository.findByName("Airport Terminal");

        if (central == null || north == null || airport == null) {
            logger.warn("Required stations not found - skipping timetable initialization");
            return;
        }

        // Find route from Central Station to North Terminal
        Route routeCTtoNT = routeRepository.findAll().stream()
            .filter(r -> r.getOriginStation().getName().equals("Central Station") &&
                       r.getDestinationStation().getName().equals("North Terminal"))
            .findFirst()
            .orElse(null);

        if (routeCTtoNT != null) {
            RouteTimetable weekdayTimetable = new RouteTimetable();
            weekdayTimetable.setRoute(routeCTtoNT);
            weekdayTimetable.setName("Weekday Morning Schedule");
            weekdayTimetable.setDescription("Morning commuter departures Monday to Friday");
            weekdayTimetable.setEffectiveFrom(LocalDate.of(2025, 1, 1));
            weekdayTimetable.setStatus(multitier.trans.model.enums.TimetableStatus.ACTIVE);
            weekdayTimetable = timetableRepository.save(weekdayTimetable);

            // Add weekday entries
            List<DayOfWeek> weekdays = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 
                                                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
            for (DayOfWeek day : weekdays) {
                RouteTimetableEntry entry1 = new RouteTimetableEntry();
                entry1.setTimetable(weekdayTimetable);
                entry1.setServiceDay(day);
                entry1.setDepartureTime(LocalTime.of(7, 30));
                entry1.setArrivalTime(LocalTime.of(8, 15));
                entry1.setNotes("Morning commuter express");
                timetableEntryRepository.save(entry1);
                
                RouteTimetableEntry entry2 = new RouteTimetableEntry();
                entry2.setTimetable(weekdayTimetable);
                entry2.setServiceDay(day);
                entry2.setDepartureTime(LocalTime.of(18, 0));
                entry2.setArrivalTime(LocalTime.of(18, 45));
                entry2.setNotes("Evening return service");
                timetableEntryRepository.save(entry2);
            }
        }

        // Find route from Central Station to Airport Terminal
        Route routeCTtoAirport = routeRepository.findAll().stream()
            .filter(r -> r.getOriginStation().getName().equals("Central Station") &&
                       r.getDestinationStation().getName().equals("Airport Terminal"))
            .findFirst()
            .orElse(null);

        if (routeCTtoAirport != null) {
            RouteTimetable weekendTimetable = new RouteTimetable();
            weekendTimetable.setRoute(routeCTtoAirport);
            weekendTimetable.setName("Weekend Airport Shuttle");
            weekendTimetable.setDescription("Frequent airport runs on weekends");
            weekendTimetable.setEffectiveFrom(LocalDate.of(2025, 1, 1));
            weekendTimetable.setStatus(multitier.trans.model.enums.TimetableStatus.ACTIVE);
            weekendTimetable = timetableRepository.save(weekendTimetable);

            // Add weekend entries
            List<DayOfWeek> weekendDays = List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
            List<LocalTime> times = List.of(
                LocalTime.of(6, 0), LocalTime.of(10, 0), LocalTime.of(14, 0), LocalTime.of(18, 0)
            );
            for (DayOfWeek day : weekendDays) {
                for (LocalTime departure : times) {
                    LocalTime arrival = departure.plus(50, ChronoUnit.MINUTES);
                    RouteTimetableEntry entry = new RouteTimetableEntry();
                    entry.setTimetable(weekendTimetable);
                    entry.setServiceDay(day);
                    entry.setDepartureTime(departure);
                    entry.setArrivalTime(arrival);
                    entry.setNotes(
                        departure.getHour() < 10 ? "Early shuttle" : 
                        departure.getHour() < 14 ? "Mid-morning shuttle" :
                        departure.getHour() < 18 ? "Afternoon shuttle" : "Evening shuttle"
                    );
                    timetableEntryRepository.save(entry);
                }
            }
        }

        logger.info("Inserted sample route timetables");
    }

    private void initializeFarePolicies() {
        if (farePolicyRepository.count() > 0) {
            logger.info("Fare policies table already contains data - skipping sample fare policy insertion");
            return;
        }

        logger.info("Inserting sample fare policies...");

        List<Route> routes = routeRepository.findAll();
        if (routes.isEmpty()) {
            logger.warn("No routes found - skipping fare policy initialization");
            return;
        }

        for (Route route : routes) {
            // Use a simple base price (can be customized per route)
            BigDecimal basePrice = BigDecimal.valueOf(25.00);

            // Create fare policies for all passenger categories and vehicle classes
            for (PassengerCategory category : PassengerCategory.values()) {
                for (VehicleClass vehicleClass : VehicleClass.values()) {
                    FarePolicy policy = new FarePolicy();
                    policy.setRoute(route);
                    policy.setPassengerCategory(category);
                    policy.setVehicleClass(vehicleClass);
                    
                    // Apply discount based on category
                    double discount = switch (category) {
                        case CHILD -> 0.25;
                        case SENIOR -> 0.30;
                        case STUDENT -> 0.20;
                        default -> 0.0;
                    };
                    
                    BigDecimal discountedPrice = basePrice.multiply(BigDecimal.valueOf(1 - discount));
                    policy.setPrice(discountedPrice);
                    policy.setEffectiveFrom(LocalDate.now());
                    policy.setStatus(multitier.trans.model.enums.PolicyStatus.ACTIVE);
                    
                    farePolicyService.saveFarePolicy(policy);
                }
            }
        }

        logger.info("Inserted sample fare policies");
    }

    private void initializeReservations() {
        if (reservationRepository.count() > 0) {
            logger.info("Reservations table already contains data - skipping sample reservation insertion");
            return;
        }

        logger.info("Inserting sample reservations...");

        List<Route> routes = routeRepository.findAll();
        if (routes.isEmpty()) {
            logger.warn("No routes found - skipping reservation initialization");
            return;
        }

        // Get or create a test user for reservations
        User testUser = userRepository.findByUsername("testuser").orElse(null);
        if (testUser == null) {
            // Create a test user if it doesn't exist
            testUser = new multitier.trans.model.RegularUser("testuser", "test@example.com", 
                passwordEncoder.encode("password123"));
            testUser.setFirstName("Test");
            testUser.setLastName("User");
            testUser = userRepository.save(testUser);
            logger.info("Created test user for sample reservations");
        }

        Route firstRoute = routes.get(0);
        PassengerCategory[] categories = PassengerCategory.values();
        VehicleClass[] vehicleClasses = VehicleClass.values();

        for (int i = 1; i <= 5; i++) {
            Reservation reservation = new Reservation();
            reservation.setUser(testUser);
            reservation.setRoute(firstRoute);
            reservation.setPassengerName("Passenger " + i);
            reservation.setPassengerEmail("passenger" + i + "@example.com");
            reservation.setPassengerPhone("+1234567890" + i);
            reservation.setSeatCount(1 + (i % 3));
            reservation.setStatus(i <= 3 ? multitier.trans.model.enums.ReservationStatus.CONFIRMED : multitier.trans.model.enums.ReservationStatus.PENDING);
            reservation.setPassengerCategory(categories[i % categories.length]);
            reservation.setVehicleClass(vehicleClasses[i % vehicleClasses.length]);
            
            LocalDateTime departureTime = LocalDateTime.now()
                .plusDays(i)
                .withHour(9)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
            
            LocalDateTime arrivalTime = departureTime.plusMinutes(45);
            
            // Use TripTimeDetails embedded object
            TripTimeDetails tripDetails = new TripTimeDetails(departureTime, arrivalTime);
            reservation.setTripDetails(tripDetails);
            
            reservationRepository.save(reservation);
        }

        logger.info("Inserted 5 sample reservations");
    }
}

