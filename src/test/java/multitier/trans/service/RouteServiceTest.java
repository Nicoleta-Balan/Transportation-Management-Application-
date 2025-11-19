package multitier.trans.service;

import multitier.trans.dto.CreateRouteRequest;
import multitier.trans.model.Route;
import multitier.trans.model.Station;
import multitier.trans.model.enums.StationStatus;
import multitier.trans.repository.RouteRepository;
import multitier.trans.repository.StationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * JUnit Test for RouteService (Business Service).
 * 
 * Unit test using mocks to test business logic including validation rules.
 */
@ExtendWith(MockitoExtension.class)
public class RouteServiceTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private StationRepository stationRepository;

    @InjectMocks
    private RouteServiceImpl routeService;

    private Station originStation;
    private Station destinationStation;
    private CreateRouteRequest createRouteRequest;

    @BeforeEach
    void setUp() {
        originStation = new Station("Origin", "Origin Description", StationStatus.ACTIVE);
        originStation.setId(1L);
        
        destinationStation = new Station("Destination", "Destination Description", StationStatus.ACTIVE);
        destinationStation.setId(2L);

        createRouteRequest = new CreateRouteRequest();
        createRouteRequest.setOriginStationId(1L);
        createRouteRequest.setDestinationStationId(2L);
        createRouteRequest.setVehicleCapacity(50);
    }

    /**
     * Test: Create route with valid data.
     */
    @Test
    public void whenCreateRoute_withValidData_thenRouteIsCreated() {
        // Arrange
        when(stationRepository.findById(1L)).thenReturn(Optional.of(originStation));
        when(stationRepository.findById(2L)).thenReturn(Optional.of(destinationStation));
        
        Route savedRoute = new Route(originStation, destinationStation, 50);
        savedRoute.setId(1L);
        when(routeRepository.save(any(Route.class))).thenReturn(savedRoute);

        // Act
        Route result = routeService.createRoute(createRouteRequest);

        // Assert
        assertNotNull(result);
        verify(stationRepository, times(1)).findById(1L);
        verify(stationRepository, times(1)).findById(2L);
        verify(routeRepository, times(1)).save(any(Route.class));
    }

    /**
     * Test: Create route fails when origin station not found.
     */
    @Test
    public void whenCreateRoute_withInvalidOriginStation_thenThrowsException() {
        // Arrange
        when(stationRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            routeService.createRoute(createRouteRequest);
        });

        assertTrue(exception.getMessage().contains("Station not found"));
        verify(routeRepository, never()).save(any(Route.class));
    }

    /**
     * Test: Create route fails when destination station not found.
     */
    @Test
    public void whenCreateRoute_withInvalidDestinationStation_thenThrowsException() {
        // Arrange
        when(stationRepository.findById(1L)).thenReturn(Optional.of(originStation));
        when(stationRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            routeService.createRoute(createRouteRequest);
        });

        assertTrue(exception.getMessage().contains("Station not found"));
        verify(routeRepository, never()).save(any(Route.class));
    }

    /**
     * Test: Create route fails when origin and destination are the same.
     */
    @Test
    public void whenCreateRoute_withSameOriginAndDestination_thenThrowsException() {
        // Arrange
        createRouteRequest.setDestinationStationId(1L); // Same as origin
        when(stationRepository.findById(1L)).thenReturn(Optional.of(originStation));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            routeService.createRoute(createRouteRequest);
        });

        assertTrue(exception.getMessage().contains("Origin and destination stations cannot be the same"));
        verify(routeRepository, never()).save(any(Route.class));
    }

    /**
     * Test: Find all routes.
     */
    @Test
    public void whenFindAllRoutes_thenRepositoryFindAllIsCalled() {
        // Act
        routeService.findAllRoutes();

        // Assert
        verify(routeRepository, times(1)).findAll();
    }

    /**
     * Test: Find route by ID.
     */
    @Test
    public void whenFindRouteById_thenRepositoryFindByIdIsCalled() {
        // Arrange
        Route route = new Route(originStation, destinationStation, 50);
        route.setId(1L);
        when(routeRepository.findById(1L)).thenReturn(Optional.of(route));

        // Act
        Optional<Route> result = routeService.findRouteById(1L);

        // Assert
        assertTrue(result.isPresent());
        verify(routeRepository, times(1)).findById(1L);
    }
}

