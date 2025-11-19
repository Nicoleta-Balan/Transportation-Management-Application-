package multitier.trans.service;

import multitier.trans.model.Route;
import multitier.trans.model.RouteStatistics;
import multitier.trans.model.Station;
import multitier.trans.repository.ReservationRepository;
import multitier.trans.repository.RouteRepository;
import multitier.trans.repository.RouteStatisticsRepository;
import multitier.trans.repository.StationRepository;
import multitier.trans.dto.CreateRouteRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * This is the "Implementation" of the RouteService.
 * It contains the actual business logic ("workflow")
 * It depends on the repositories to talk to the database.
 * Uses Spring Data JPA repositories for all data access operations.
 * Replaces database triggers for deletion prevention and route statistics creation.
 */

@Service
@Transactional
public class RouteServiceImpl implements RouteService { // Implementează interfața

    // Service-ul depinde de ambele repository-uri
    private final RouteRepository routeRepository;
    private final StationRepository stationRepository;
    private final ReservationRepository reservationRepository;
    private final RouteStatisticsRepository routeStatisticsRepository;

    // constructor injection
    @Autowired
    public RouteServiceImpl(RouteRepository routeRepository, 
                           StationRepository stationRepository,
                           ReservationRepository reservationRepository,
                           RouteStatisticsRepository routeStatisticsRepository) {
        this.routeRepository = routeRepository;
        this.stationRepository = stationRepository;
        this.reservationRepository = reservationRepository;
        this.routeStatisticsRepository = routeStatisticsRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Route> findAllRoutes() {
        return routeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Route> findRouteById(Long id) {
        return routeRepository.findById(id);
    }

    /**
     * This is the business logic implementation.
     * 1. Receives the DTO request.
     * 2. Finds the origin station by its ID.
     * 3. Finds the destination station by its ID.
     * 4. Creates a new Route object using the found stations.
     * 5. Saves and returns the new Route.
     */

    @Override
    public Route createRoute(CreateRouteRequest request) {

        // First Step: find the origin station
        Station origin = stationRepository.findById(request.getOriginStationId())
                .orElseThrow(() -> new RuntimeException("Station not found with id: " + request.getOriginStationId()));

        // Second Step: find the destination station
        Station destination = stationRepository.findById(request.getDestinationStationId())
                .orElseThrow(() -> new RuntimeException("Station not found with id: " + request.getDestinationStationId()));

        // Business rule validation: Prevent circular routes (origin and destination cannot be the same)
        // This replaces database trigger validate_route_creation()
        if (origin.getId().equals(destination.getId())) {
            throw new RuntimeException("Route cannot have the same origin and destination station (circular route not allowed)");
        }

        // Business rule validation: Vehicle capacity must be positive
        if (request.getVehicleCapacity() <= 0) {
            throw new RuntimeException("Vehicle capacity must be greater than 0");
        }

        // Create the new object Route
        Route newRoute = new Route(origin, destination, request.getVehicleCapacity());

        // Save it in the database
        Route savedRoute = routeRepository.save(newRoute);

        // Create route statistics (replaces database trigger create_route_statistics)
        createRouteStatistics(savedRoute.getId());

        return savedRoute;
    }

    @Override
    public void deleteRoute(Long routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found with id: " + routeId));

        // Check if route has reservations
        List<multitier.trans.model.Reservation> reservations = reservationRepository.findByRouteId(routeId);
        if (!reservations.isEmpty()) {
            throw new RuntimeException("Cannot delete route " + routeId + 
                    " because it has reservations");
        }

        routeRepository.delete(route);
    }

    /**
     * Creates route statistics for a new route.
     * This replaces the database trigger create_route_statistics().
     */
    private void createRouteStatistics(Long routeId) {
        // Check if statistics already exist (idempotent)
        if (routeStatisticsRepository.existsById(routeId)) {
            return;
        }

        RouteStatistics statistics = new RouteStatistics();
        statistics.setRouteId(routeId);
        statistics.setTotalReservations(0);
        statistics.setConfirmedReservations(0);
        statistics.setCancelledReservations(0);
        statistics.setTotalRevenue(BigDecimal.ZERO);
        statistics.setAverageOccupancyRate(BigDecimal.ZERO);
        statistics.setLastCalculated(LocalDateTime.now());

        routeStatisticsRepository.save(statistics);
    }
}