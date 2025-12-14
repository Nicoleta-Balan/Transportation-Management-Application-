package multitier.trans.service;

import multitier.trans.factory.RouteFactory;
import multitier.trans.model.Route;
import multitier.trans.model.RouteStop;
import multitier.trans.repository.RouteRepository;
import multitier.trans.repository.StationRepository;
import multitier.trans.dto.CreateRouteRequest;
import multitier.trans.dto.UpdateRouteRequest;
import multitier.trans.dto.RouteStopRequest;
import multitier.trans.utils.RepositoryUtils;
import multitier.trans.utils.ValidationUtils;
import multitier.trans.utils.StopCreationHelper;
import multitier.trans.utils.RouteStopCreationStrategy;
import multitier.trans.utils.JpaUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

@Service
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;
    private final EntityManager entityManager;
    private final RouteFactory routeFactory;
    private final StopCreationHelper<RouteStop, RouteStopRequest, Route> stopCreationHelper;

    // Custom constructor needed for stopCreationHelper initialization
    public RouteServiceImpl(
            RouteRepository routeRepository, 
            StationRepository stationRepository,
            EntityManager entityManager,
            RouteFactory routeFactory) {
        this.routeRepository = routeRepository;
        this.entityManager = entityManager;
        this.routeFactory = routeFactory;
        this.stopCreationHelper = new StopCreationHelper<>(
                new RouteStopCreationStrategy(),
                stationRepository,
                RouteStopRequest::getStationId,
                RouteStopRequest::getSequenceOrder
        );
    }

    @Override
    public List<Route> findAllRoutes() {
        // Use custom query to eagerly load routeStops
        return routeRepository.findAllWithStops();
    }

    @Override
    public Optional<Route> findRouteById(Long id) {
        // Use custom query to eagerly load routeStops
        return routeRepository.findByIdWithStops(id);
    }

    @Override
    public Route getRouteById(Long id) {
        return RepositoryUtils.findByIdOrThrow(
            routeRepository.findByIdWithStops(id),
            "Route",
            id
        );
    }

    @Override
    @Transactional
    public Route createRoute(CreateRouteRequest request) {
        // Create route using factory
        Route route = routeFactory.createRoute(request);

        // Save route (stops will be saved via cascade)
        Route savedRoute = routeRepository.save(route);
        
        // Flush to ensure stops are persisted immediately
        // This forces JPA to execute the cascade operations
        entityManager.flush();
        
        // Reload with stops to ensure they're included in response
        // This ensures the collection is properly loaded from the database
        Route reloaded = reloadRouteWithStops(savedRoute.getId());
        
        // Force initialization of the collection within the transaction
        // This ensures Jackson can serialize it even after the transaction ends
        // Accessing the collection and its elements forces Hibernate to load them
        List<RouteStop> stops = reloaded.getRouteStops();
        if (stops != null && !stops.isEmpty()) {
            // Access each stop to ensure they're fully loaded
            stops.forEach(stop -> {
                stop.getId(); // Access ID
                stop.getStation(); // Access station (EAGER, but ensures it's loaded)
                stop.getSequenceOrder(); // Access sequence order
            });
        }

        return reloaded;
    }

    @Override
    @Transactional
    public Route updateRoute(Long id, UpdateRouteRequest request) {
        // Validate stops
        ValidationUtils.validateSequenceOrder(
            request.getStops(),
            2,
            "Route",
            RouteStopRequest::getSequenceOrder
        );

        // Load route with stops to properly manage the collection
        Route existingRoute = RepositoryUtils.findByIdOrThrow(
            routeRepository.findByIdWithStops(id),
            "Route",
            id
        );

        // Update route basic fields manually
        existingRoute.setVehicleClass(request.getVehicleClass()); // This also sets vehicleCapacity via custom setter
        existingRoute.setDistance(request.getDistance());
        existingRoute.setDurationMinutes(request.getDurationMinutes());
        // Normalize description: convert empty string to null for JPA
        String normalizedDescription = (request.getDescription() != null && request.getDescription().trim().isEmpty()) 
            ? null 
            : request.getDescription();
        existingRoute.setDescription(normalizedDescription);
        
        // Check if route stops have changed by comparing existing stops with new ones
        boolean stopsChanged = areStopsDifferent(existingRoute.getRouteStops(), request.getStops());
        
        if (stopsChanged) {
            // Delete old stops first to avoid unique constraint violations
            // This ensures old stops are removed before new ones are inserted
            JpaUtils.clearCollectionAndFlush(existingRoute.getRouteStops(), entityManager);
            
            // Create new route stops with cumulative calculations
            List<RouteStop> newStops = createRouteStops(existingRoute, request.getStops());
            
            // Add new stops to the collection
            existingRoute.getRouteStops().addAll(newStops);
        }

        // Save route (stops will be saved via cascade)
        Route updatedRoute = routeRepository.save(existingRoute);

        // Reload with stops to ensure they're included in response
        return reloadRouteWithStops(updatedRoute.getId());
    }

    private Route reloadRouteWithStops(Long routeId) {
        return RepositoryUtils.reloadWithStops(
            routeRepository::findByIdWithStops,
            "Route",
            routeId
        );
    }

    private List<RouteStop> createRouteStops(Route route, List<RouteStopRequest> stopRequests) {
        return stopCreationHelper.createStops(route, stopRequests);
    }

    private boolean areStopsDifferent(List<RouteStop> existingStops, List<RouteStopRequest> newStops) {
        // If sizes differ, stops have changed
        if (existingStops == null || newStops == null) {
            return existingStops == null && newStops == null ? false : true;
        }
        
        if (existingStops.size() != newStops.size()) {
            return true;
        }
        
        // Compare each stop by position
        for (int i = 0; i < existingStops.size(); i++) {
            RouteStop existing = existingStops.get(i);
            RouteStopRequest newStop = newStops.get(i);
            
            // Compare station ID
            if (!existing.getStation().getId().equals(newStop.getStationId())) {
                return true;
            }
            
            // Compare sequence order
            if (!existing.getSequenceOrder().equals(newStop.getSequenceOrder())) {
                return true;
            }
            
            // Compare distance from previous (with small tolerance for floating point)
            if (Math.abs(existing.getDistanceFromPrevious() - newStop.getDistanceFromPrevious()) > 0.01) {
                return true;
            }
            
            // Compare duration from previous
            if (!existing.getDurationMinutesFromPrevious().equals(newStop.getDurationMinutesFromPrevious())) {
                return true;
            }
        }
        
        // All stops are the same
        return false;
    }

    @Override
    public void deleteRoute(Long id) {
        RepositoryUtils.deleteByIdOrThrow(routeRepository, "Route", id);
    }
}