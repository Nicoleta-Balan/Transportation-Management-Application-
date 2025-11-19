package multitier.trans.repository;

import multitier.trans.model.Route;
import multitier.trans.model.Station;
import multitier.trans.model.enums.StationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit Test for RouteRepository (JPA Repository Service).
 * 
 * Integration test for Route entity repository operations.
 * Tests CRUD operations and relationships with Station entities.
 */
@DataJpaTest
public class RouteRepositoryTest {

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private StationRepository stationRepository;

    private Station originStation;
    private Station destinationStation;
    private Route testRoute;

    @BeforeEach
    void setUp() {
        // Create test stations
        originStation = new Station("Origin Station", "Origin Description", StationStatus.ACTIVE);
        destinationStation = new Station("Destination Station", "Destination Description", StationStatus.ACTIVE);
        
        // Save stations first (required for foreign key relationship)
        originStation = stationRepository.save(originStation);
        destinationStation = stationRepository.save(destinationStation);
        
        // Create test route
        testRoute = new Route(originStation, destinationStation, 50);
    }

    /**
     * Test: Save a new route and verify it's persisted correctly.
     */
    @Test
    public void whenSaveRoute_thenRouteIsPersisted() {
        // Act
        Route savedRoute = routeRepository.save(testRoute);

        // Assert
        assertNotNull(savedRoute.getId(), "Route should have an ID after saving");
        assertEquals(originStation.getId(), savedRoute.getOriginStation().getId());
        assertEquals(destinationStation.getId(), savedRoute.getDestinationStation().getId());
        assertEquals(50, savedRoute.getVehicleCapacity());
    }

    /**
     * Test: Find route by ID.
     */
    @Test
    public void whenFindById_thenReturnRoute() {
        // Arrange
        Route savedRoute = routeRepository.save(testRoute);
        Long routeId = savedRoute.getId();

        // Act
        Optional<Route> foundRoute = routeRepository.findById(routeId);

        // Assert
        assertTrue(foundRoute.isPresent(), "Route should be found");
        assertEquals(50, foundRoute.get().getVehicleCapacity());
        assertNotNull(foundRoute.get().getOriginStation());
        assertNotNull(foundRoute.get().getDestinationStation());
    }

    /**
     * Test: Find all routes.
     */
    @Test
    public void whenFindAll_thenReturnAllRoutes() {
        // Arrange
        Route route1 = new Route(originStation, destinationStation, 50);
        Route route2 = new Route(destinationStation, originStation, 30);
        routeRepository.save(route1);
        routeRepository.save(route2);

        // Act
        List<Route> allRoutes = routeRepository.findAll();

        // Assert
        assertTrue(allRoutes.size() >= 2, "Should find at least 2 routes");
    }

    /**
     * Test: Update route.
     */
    @Test
    public void whenUpdateRoute_thenRouteIsUpdated() {
        // Arrange
        Route savedRoute = routeRepository.save(testRoute);
        Long routeId = savedRoute.getId();

        // Act
        savedRoute.setVehicleCapacity(100);
        Route updatedRoute = routeRepository.save(savedRoute);

        // Assert
        assertEquals(100, updatedRoute.getVehicleCapacity());
        assertEquals(routeId, updatedRoute.getId(), "ID should remain the same");
    }

    /**
     * Test: Delete route.
     */
    @Test
    public void whenDeleteRoute_thenRouteIsRemoved() {
        // Arrange
        Route savedRoute = routeRepository.save(testRoute);
        Long routeId = savedRoute.getId();

        // Act
        routeRepository.delete(savedRoute);

        // Assert
        Optional<Route> deletedRoute = routeRepository.findById(routeId);
        assertFalse(deletedRoute.isPresent(), "Route should be deleted");
    }

    /**
     * Test: Verify route relationships with stations are maintained.
     */
    @Test
    public void whenSaveRoute_thenStationRelationshipsAreMaintained() {
        // Act
        Route savedRoute = routeRepository.save(testRoute);

        // Assert
        assertNotNull(savedRoute.getOriginStation(), "Origin station should be set");
        assertNotNull(savedRoute.getDestinationStation(), "Destination station should be set");
        assertEquals("Origin Station", savedRoute.getOriginStation().getName());
        assertEquals("Destination Station", savedRoute.getDestinationStation().getName());
    }
}

