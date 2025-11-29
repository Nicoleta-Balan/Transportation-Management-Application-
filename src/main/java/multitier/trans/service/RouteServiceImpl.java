package multitier.trans.service;

import multitier.trans.model.Route;
import multitier.trans.model.Station;
import multitier.trans.repository.RouteRepository;
import multitier.trans.repository.StationRepository;
import multitier.trans.dto.CreateRouteRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * This is the "Implementation" of the RouteService.
 * It contains the actual business logic ("workflow")
 * It depends on the repositories to talk to the database.
 */

@Service
public class RouteServiceImpl implements RouteService { // Implementează interfața

    // Service-ul depinde de ambele repository-uri
    private final RouteRepository routeRepository;
    private final StationRepository stationRepository;

    // constructor injection
    @Autowired
    public RouteServiceImpl(RouteRepository routeRepository, StationRepository stationRepository) {
        this.routeRepository = routeRepository;
        this.stationRepository = stationRepository;
    }

    @Override
    public List<Route> findAllRoutes() {
        return routeRepository.findAll();
    }

    @Override
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

        // Additional Validation
        if (origin.getId().equals(destination.getId())) {
            throw new RuntimeException("Origin and destination stations cannot be the same.");
        }

        // Create the new object Route using VehicleClass to derive seat capacity
        Route newRoute = new Route(origin, destination, request.getVehicleClass());

        // Save it in the database
        return routeRepository.save(newRoute);
    }
}