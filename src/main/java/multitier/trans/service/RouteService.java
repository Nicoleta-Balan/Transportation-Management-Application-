package multitier.trans.service;

import multitier.trans.dto.CreateRouteRequest;
import multitier.trans.model.Route;
import java.util.List;
import java.util.Optional;

/**
 * Service Layer Interface for Route
 * Uses a DTO to create routes, aligning
 * .
 */
public interface RouteService {

    List<Route> findAllRoutes();

    Optional<Route> findRouteById(Long id);

    /**
     * Creates a new Route based on the DTO request.
     * This is the business logic (workflow) for creating a route.
     * @param request The DTO containing the IDs and capacity.
     * @return The newly created and saved Route entity.
     */
    Route createRoute(CreateRouteRequest request);

    /**
     * Deletes a route with validation.
     * Prevents deletion if the route has reservations.
     * 
     * @param routeId The ID of the route to delete
     * @throws RuntimeException if the route cannot be deleted
     */
    void deleteRoute(Long routeId);
}