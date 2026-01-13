package multitier.trans.controllers.rest;

import multitier.trans.dto.SimpleRouteDTO;
import multitier.trans.model.Route;
import multitier.trans.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/search")
@CrossOrigin(origins = "*")
public class RouteSearchController {

    @Autowired
    private RouteRepository routeRepository;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Search API is working! Time: " + System.currentTimeMillis());
    }

    @GetMapping("/routes")
    public ResponseEntity<List<SimpleRouteDTO>> searchRoutes(
            @RequestParam(required = false) Long fromStationId,
            @RequestParam(required = false) Long toStationId,
            @RequestParam(required = false) String date) {

        System.out.println("========================================");
        System.out.println("SEARCH REQUEST RECEIVED");
        System.out.println("From: " + fromStationId + ", To: " + toStationId + ", Date: " + date);
        System.out.println("========================================");

        List<SimpleRouteDTO> results = new ArrayList<>();

        try {
            // Get all routes with their stations
            List<Route> routes = routeRepository.findAllSimple();
            System.out.println("Found " + routes.size() + " routes in database");

            for (Route route : routes) {
                SimpleRouteDTO dto = new SimpleRouteDTO();
                dto.setId(route.getId());
                dto.setVehicleClass(route.getVehicleClass() != null ? route.getVehicleClass().name() : "STANDARD");
                dto.setDistance(route.getDistance() != null ? route.getDistance() : 0.0);
                dto.setDurationMinutes(route.getDurationMinutes() != null ? route.getDurationMinutes() : 0);

                // Get station names
                if (route.getOriginStation() != null) {
                    dto.setOriginStation(route.getOriginStation().getName());
                } else {
                    dto.setOriginStation("Unknown Origin");
                }

                if (route.getDestinationStation() != null) {
                    dto.setDestinationStation(route.getDestinationStation().getName());
                } else {
                    dto.setDestinationStation("Unknown Destination");
                }

                results.add(dto);
                System.out.println("Route " + dto.getId() + ": " + dto.getOriginStation() + " -> " + dto.getDestinationStation());
            }

        } catch (Exception e) {
            System.err.println("ERROR in search: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Returning " + results.size() + " routes");
        return ResponseEntity.ok(results);
    }
}
