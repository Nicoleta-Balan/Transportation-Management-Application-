package multitier.trans.service;

import multitier.trans.dto.CreateReservationRequest;
import multitier.trans.factory.ReservationFactory;
import multitier.trans.model.Reservation;
import multitier.trans.model.Route;
import multitier.trans.model.Station;
import multitier.trans.model.User;
import multitier.trans.model.enums.PassengerCategory;
import multitier.trans.model.enums.ReservationStatus;
import multitier.trans.model.enums.StationStatus;
import multitier.trans.model.enums.VehicleClass;
import multitier.trans.repository.ReservationRepository;
import multitier.trans.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * JUnit Test for ReservationService (Business Service).
 * 
 * Unit test using mocks to test business logic including security checks.
 */
@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private UserService userService;

    @Mock
    private ReservationFactory reservationFactory;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private Station originStation;
    private Station destinationStation;
    private Route testRoute;
    private User testUser;
    private CreateReservationRequest createRequest;
    private Reservation testReservation;

    @BeforeEach
    void setUp() {
        // Setup SecurityContext
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");

        // Create test data
        originStation = new Station("Origin", "Origin Description", StationStatus.ACTIVE);
        originStation.setId(1L);
        
        destinationStation = new Station("Destination", "Destination Description", StationStatus.ACTIVE);
        destinationStation.setId(2L);

        testRoute = new Route(originStation, destinationStation, 50);
        testRoute.setId(1L);

        testUser = new multitier.trans.model.RegularUser("testuser", "test@example.com", "hashedPassword");
        testUser.setId(1L);

        createRequest = new CreateReservationRequest();
        createRequest.setRouteId(1L);
        createRequest.setPassengerName("Test Passenger");
        createRequest.setPassengerEmail("passenger@example.com");
        createRequest.setPassengerPhone("1234567890");
        createRequest.setSeatCount(2);
        createRequest.setDepartureTime(LocalDateTime.of(2025, 12, 1, 10, 0));
        createRequest.setArrivalTime(LocalDateTime.of(2025, 12, 1, 12, 0));
        createRequest.setPassengerCategory(PassengerCategory.ADULT);
        createRequest.setVehicleClass(VehicleClass.STANDARD);

        testReservation = new Reservation();
        testReservation.setId(1L);
        testReservation.setUser(testUser);
        testReservation.setRoute(testRoute);
        testReservation.setPassengerName("Test Passenger");
        testReservation.setSeatCount(2);
        testReservation.setStatus(ReservationStatus.CONFIRMED);
        testReservation.setPassengerCategory(PassengerCategory.ADULT);
        testReservation.setVehicleClass(VehicleClass.STANDARD);
    }

    /**
     * Test: Create reservation with valid data.
     */
    @Test
    public void whenCreateReservation_withValidData_thenReservationIsCreated() {
        // Arrange
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(routeRepository.findById(1L)).thenReturn(Optional.of(testRoute));
        when(reservationFactory.createReservation(any(User.class), any(Route.class), any(CreateReservationRequest.class)))
                .thenReturn(testReservation);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));

        // Act
        Reservation result = reservationService.createReservation(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Test Passenger", result.getPassengerName());
        verify(userService, times(1)).findByUsername("testuser");
        verify(routeRepository, times(1)).findById(1L);
        verify(reservationFactory, times(1)).createReservation(any(User.class), any(Route.class), any(CreateReservationRequest.class));
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    /**
     * Test: Create reservation fails when route not found.
     */
    @Test
    public void whenCreateReservation_withInvalidRoute_thenThrowsException() {
        // Arrange
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(routeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reservationService.createReservation(createRequest);
        });

        assertTrue(exception.getMessage().contains("Route not found"));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    /**
     * Test: Get all reservations.
     */
    @Test
    public void whenGetAllReservations_thenRepositoryFindAllIsCalled() {
        // Arrange
        Reservation reservation1 = new Reservation();
        Reservation reservation2 = new Reservation();
        List<Reservation> reservations = Arrays.asList(reservation1, reservation2);
        
        when(reservationRepository.findAll()).thenReturn(reservations);

        // Act
        List<Reservation> result = reservationService.getAllReservations();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(reservationRepository, times(1)).findAll();
    }

    /**
     * Test: Get my reservations (for current user).
     */
    @Test
    public void whenGetMyReservations_thenRepositoryFindByUserIdIsCalled() {
        // Arrange
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        Reservation reservation1 = new Reservation();
        List<Reservation> reservations = Arrays.asList(reservation1);
        
        when(reservationRepository.findByUserId(1L)).thenReturn(reservations);

        // Act
        List<Reservation> result = reservationService.getMyReservations();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userService, times(1)).findByUsername("testuser");
        verify(reservationRepository, times(1)).findByUserId(1L);
    }

    /**
     * Test: Cancel reservation.
     */
    @Test
    public void whenCancelReservation_thenStatusIsUpdated() {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(authentication.getAuthorities()).thenReturn(Arrays.asList());
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        
        Reservation cancelledReservation = new Reservation();
        cancelledReservation.setId(1L);
        cancelledReservation.setStatus(ReservationStatus.CANCELLED);
        cancelledReservation.setUser(testUser);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(cancelledReservation);

        // Act
        Reservation result = reservationService.cancelReservation(1L);

        // Assert
        assertEquals(ReservationStatus.CANCELLED, result.getStatus());
        verify(reservationRepository, times(1)).findById(1L);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    /**
     * Test: Get reservation by ID.
     */
    @Test
    public void whenGetReservationById_thenRepositoryFindByIdIsCalled() {
        // Arrange
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(authentication.getAuthorities()).thenReturn(Arrays.asList());
        when(userService.findByUsername("testuser")).thenReturn(testUser);

        // Act
        Optional<Reservation> result = reservationService.getReservationById(1L);

        // Assert
        assertTrue(result.isPresent());
        verify(reservationRepository, times(1)).findById(1L);
    }
}

