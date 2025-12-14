package multitier.trans.service;

import multitier.trans.dto.CreateRouteRequest;
import multitier.trans.dto.UpdateRouteRequest;
import multitier.trans.model.Route;
import java.util.List;
import java.util.Optional;

public interface RouteService {

    List<Route> findAllRoutes();

    Optional<Route> findRouteById(Long id);

    Route getRouteById(Long id);

    Route createRoute(CreateRouteRequest request);

    Route updateRoute(Long id, UpdateRouteRequest request);

    void deleteRoute(Long id);
}