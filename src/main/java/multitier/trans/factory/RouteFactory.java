package multitier.trans.factory;

import multitier.trans.dto.CreateRouteRequest;
import multitier.trans.dto.RouteStopRequest;
import multitier.trans.model.Route;
import multitier.trans.model.RouteStop;
import multitier.trans.repository.StationRepository;
import multitier.trans.utils.StopCreationHelper;
import multitier.trans.utils.RouteStopCreationStrategy;
import multitier.trans.utils.ValidationUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RouteFactory {

    private final StopCreationHelper<RouteStop, RouteStopRequest, Route> stopCreationHelper;

    public RouteFactory(StationRepository stationRepository) {
        this.stopCreationHelper = new StopCreationHelper<>(
                new RouteStopCreationStrategy(),
                stationRepository,
                RouteStopRequest::getStationId,
                RouteStopRequest::getSequenceOrder
        );
    }

    public Route createRoute(CreateRouteRequest request) {
        // Validate stops
        ValidationUtils.validateSequenceOrder(
            request.getStops(),
            2,
            "Route",
            RouteStopRequest::getSequenceOrder
        );

        // Create route and map basic fields manually
        Route route = new Route();
        route.setVehicleClass(request.getVehicleClass()); // This also sets vehicleCapacity via custom setter
        route.setDistance(request.getDistance());
        route.setDurationMinutes(request.getDurationMinutes());
        route.setDescription(normalizeDescription(request.getDescription()));

        // Create and link route stops with cumulative calculations
        List<RouteStop> stops = stopCreationHelper.createStops(route, request.getStops());
        
        // Log to verify stops are created
        if (stops == null || stops.isEmpty()) {
            throw new IllegalStateException("No stops were created by StopCreationHelper!");
        }
        
        // Use the Route's setter method to add stops to the collection
        // This ensures JPA change tracking works correctly and the bidirectional relationship is maintained
        route.setRouteStops(stops);
        
        // Set origin and destination stations explicitly
        if (!stops.isEmpty()) {
            route.setOriginStation(stops.get(0).getStation());
            route.setDestinationStation(stops.get(stops.size() - 1).getStation());
        }
        
        // Verify stops were added to the collection
        if (route.getRouteStops().size() != stops.size()) {
            throw new IllegalStateException(
                String.format("Stops were not added correctly! Expected %d stops, but collection has %d", 
                    stops.size(), route.getRouteStops().size()));
        }

        return route;
    }

    private String normalizeDescription(String description) {
        if (description != null && description.trim().isEmpty()) {
            return null;
        }
        return description;
    }
}
