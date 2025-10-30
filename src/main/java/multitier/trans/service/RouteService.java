package multitier.trans.service;

import multitier.trans.dto.CreateRouteRequest;
import multitier.trans.model.Route;
import java.util.List;
import java.util.Optional;

/**
 * Service Layer Interface for Route
 * This "complex" version uses a DTO to create routes, aligning
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
}