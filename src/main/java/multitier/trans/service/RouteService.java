package multitier.trans.service;

import multitier.trans.dto.CreateRouteRequest;
import multitier.trans.model.Route;
import java.util.List;
import java.util.Optional;

// Service Layer Interface for Route

public interface RouteService {

    List<Route> findAllRoutes();

    Optional<Route> findRouteById(Long id);


// This is the business logic (workflow) for creating a route.

    Route createRoute(CreateRouteRequest request);
}