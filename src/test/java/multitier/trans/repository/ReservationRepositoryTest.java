package multitier.trans.repository;

import multitier.trans.model.Reservation;
import multitier.trans.model.Route;
import multitier.trans.model.Station;
import multitier.trans.model.TripTimeDetails;
import multitier.trans.model.User;
import multitier.trans.model.enums.PassengerCategory;
import multitier.trans.model.enums.ReservationStatus;
import multitier.trans.model.enums.StationStatus;
import multitier.trans.model.enums.VehicleClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit Test for ReservationRepository (JPA Repository Service).
 * 
 * Integration test for Reservation entity repository operations.
 * Tests CRUD operations, custom query methods, and relationships.
 */
@DataJpaTest
public class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private UserRepository userRepository;

    private Station originStation;
    private Station destinationStation;
    private Route testRoute;
    private User testUser;
    private Reservation testReservation;

    @BeforeEach
    void setUp() {
        // Create and save stations
        originStation = new Station("Origin", "Origin Description", StationStatus.ACTIVE);
        destinationStation = new Station("Destination", "Destination Description", StationStatus.ACTIVE);
        originStation = stationRepository.save(originStation);
        destinationStation = stationRepository.save(destinationStation);

        // Create and save route
        testRoute = new Route(originStation, destinationStation, 50);
        testRoute = routeRepository.save(testRoute);

        // Create and save user (using RegularUser which extends User)
        testUser = new multitier.trans.model.RegularUser("testuser", "test@example.com", "hashedPassword");
        testUser = userRepository.save(testUser);

        // Create test reservation
        LocalDateTime departureTime = LocalDateTime.of(2025, 12, 1, 10, 0);
        LocalDateTime arrivalTime = LocalDateTime.of(2025, 12, 1, 12, 0);
        
        testReservation = new Reservation();
        testReservation.setUser(testUser);
        testReservation.setRoute(testRoute);
        testReservation.setPassengerName("Test Passenger");
        testReservation.setPassengerEmail("passenger@example.com");
        testReservation.setPassengerPhone("1234567890");
        testReservation.setSeatCount(2);
        testReservation.setTripDetails(new TripTimeDetails(departureTime, arrivalTime));
        testReservation.setStatus(ReservationStatus.CONFIRMED);
        testReservation.setPassengerCategory(PassengerCategory.ADULT);
        testReservation.setVehicleClass(VehicleClass.STANDARD);
    }

    /**
     * Test: Save a new reservation and verify it's persisted correctly.
     */
    @Test
    public void whenSaveReservation_thenReservationIsPersisted() {
        // Act
        Reservation savedReservation = reservationRepository.save(testReservation);

        // Assert
        assertNotNull(savedReservation.getId(), "Reservation should have an ID after saving");
        assertEquals("Test Passenger", savedReservation.getPassengerName());
        assertEquals(2, savedReservation.getSeatCount());
        assertEquals(ReservationStatus.CONFIRMED, savedReservation.getStatus());
        assertEquals(PassengerCategory.ADULT, savedReservation.getPassengerCategory());
        assertEquals(VehicleClass.STANDARD, savedReservation.getVehicleClass());
        assertNotNull(savedReservation.getUser());
        assertNotNull(savedReservation.getRoute());
    }

    /**
     * Test: Find reservation by ID.
     */
    @Test
    public void whenFindById_thenReturnReservation() {
        // Arrange
        Reservation savedReservation = reservationRepository.save(testReservation);
        Long reservationId = savedReservation.getId();

        // Act
        Optional<Reservation> foundReservation = reservationRepository.findById(reservationId);

        // Assert
        assertTrue(foundReservation.isPresent(), "Reservation should be found");
        assertEquals("Test Passenger", foundReservation.get().getPassengerName());
    }

    /**
     * Test: Find reservations by route ID (custom query method).
     */
    @Test
    public void whenFindByRouteId_thenReturnReservationsForRoute() {
        // Arrange
        reservationRepository.save(testReservation);
        
        // Create another reservation for the same route
        Reservation reservation2 = new Reservation();
        reservation2.setUser(testUser);
        reservation2.setRoute(testRoute);
        reservation2.setPassengerName("Another Passenger");
        reservation2.setSeatCount(1);
        reservation2.setTripDetails(new TripTimeDetails(
            LocalDateTime.of(2025, 12, 1, 14, 0),
            LocalDateTime.of(2025, 12, 1, 16, 0)
        ));
        reservation2.setStatus(ReservationStatus.CONFIRMED);
        reservation2.setPassengerCategory(PassengerCategory.ADULT);
        reservation2.setVehicleClass(VehicleClass.STANDARD);
        reservationRepository.save(reservation2);

        // Act
        List<Reservation> reservations = reservationRepository.findByRouteId(testRoute.getId());

        // Assert
        assertTrue(reservations.size() >= 2, "Should find at least 2 reservations");
        assertTrue(reservations.stream().anyMatch(r -> r.getPassengerName().equals("Test Passenger")));
        assertTrue(reservations.stream().anyMatch(r -> r.getPassengerName().equals("Another Passenger")));
    }

    /**
     * Test: Find reservations by user ID (custom query method).
     */
    @Test
    public void whenFindByUserId_thenReturnReservationsForUser() {
        // Arrange
        reservationRepository.save(testReservation);

        // Act
        List<Reservation> reservations = reservationRepository.findByUserId(testUser.getId());

        // Assert
        assertTrue(reservations.size() >= 1, "Should find at least 1 reservation");
        assertTrue(reservations.stream().anyMatch(r -> r.getPassengerName().equals("Test Passenger")));
    }

    /**
     * Test: Find reservations by passenger name (custom query method).
     */
    @Test
    public void whenFindByPassengerName_thenReturnReservationsForPassenger() {
        // Arrange
        reservationRepository.save(testReservation);

        // Act
        List<Reservation> reservations = reservationRepository.findByPassengerName("Test Passenger");

        // Assert
        assertTrue(reservations.size() >= 1, "Should find at least 1 reservation");
        assertEquals("Test Passenger", reservations.get(0).getPassengerName());
    }

    /**
     * Test: Update reservation.
     */
    @Test
    public void whenUpdateReservation_thenReservationIsUpdated() {
        // Arrange
        Reservation savedReservation = reservationRepository.save(testReservation);
        Long reservationId = savedReservation.getId();

        // Act
        savedReservation.setStatus(ReservationStatus.CANCELLED);
        Reservation updatedReservation = reservationRepository.save(savedReservation);

        // Assert
        assertEquals(ReservationStatus.CANCELLED, updatedReservation.getStatus());
        assertEquals(reservationId, updatedReservation.getId(), "ID should remain the same");
    }

    /**
     * Test: Delete reservation.
     */
    @Test
    public void whenDeleteReservation_thenReservationIsRemoved() {
        // Arrange
        Reservation savedReservation = reservationRepository.save(testReservation);
        Long reservationId = savedReservation.getId();

        // Act
        reservationRepository.delete(savedReservation);

        // Assert
        Optional<Reservation> deletedReservation = reservationRepository.findById(reservationId);
        assertFalse(deletedReservation.isPresent(), "Reservation should be deleted");
    }

    /**
     * Test: Verify reservation relationships with user and route are maintained.
     */
    @Test
    public void whenSaveReservation_thenRelationshipsAreMaintained() {
        // Act
        Reservation savedReservation = reservationRepository.save(testReservation);

        // Assert
        assertNotNull(savedReservation.getUser(), "User should be set");
        assertNotNull(savedReservation.getRoute(), "Route should be set");
        assertEquals(testUser.getId(), savedReservation.getUser().getId());
        assertEquals(testRoute.getId(), savedReservation.getRoute().getId());
    }

    /**
     * Test: Verify enum values are persisted correctly.
     */
    @Test
    public void whenSaveReservation_thenEnumValuesArePersisted() {
        // Act
        Reservation savedReservation = reservationRepository.save(testReservation);

        // Refresh from database
        Reservation foundReservation = reservationRepository.findById(savedReservation.getId())
                .orElseThrow();

        // Assert
        assertEquals(PassengerCategory.ADULT, foundReservation.getPassengerCategory());
        assertEquals(VehicleClass.STANDARD, foundReservation.getVehicleClass());
    }
}

