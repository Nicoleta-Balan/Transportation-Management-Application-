package multitier.trans.controllers;
import multitier.trans.dto.CreateRouteRequest; // Import the DTO
import multitier.trans.model.Route;
import multitier.trans.service.RouteService; // Import the Service Interface
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid; // Import @Valid for validation
import java.util.List;

/**
 * REST Controller to handle HTTP requests for managing routes (the API endpoints).
 *
 * - This controller now depends on the 'RouteService' (the business logic layer)
 * instead of directly on the 'RouteRepository'.
 * - The create endpoint now accepts a 'CreateRouteRequest' DTO.
 */

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    //The controller now depends on the Service layer, not the repository.
    @Autowired
    private RouteService routeService;

    /**
     * GET endpoint (Read All): Fetches all routes.
     * Now delegates the call to the service layer.
     */

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Route> getAllRoutes() {
        // Now calls the service
        return routeService.findAllRoutes();
    }

    /**
     * GET endpoint (Read One): Fetches a single route by its ID.
     * Delegates the call to the service layer.
     */

    @GetMapping("/{id}")
    public ResponseEntity<Route> getRouteById(@PathVariable Long id) {
        // The service handles the logic of finding the route
        return routeService.findRouteById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST endpoint (Create): Creates a new route.
     * 1. The @RequestBody is now a 'CreateRouteRequest' DTO (the "form").
     * 2. The @Valid annotation tells Spring to check the validation rules
     * on the DTO (e.g., @NotNull, @Min).
     * 3. All the complex creation logic is delegated to the 'routeService'.
     */

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Route> createRoute(@Valid @RequestBody CreateRouteRequest request) {

        // The controller's only job is to pass the "form" (DTO)
        // to the "brain" (Service).
        Route createdRoute;
        createdRoute = routeService.createRoute(request);

        // Return a 201 Created status, which is the correct HTTP response
        return ResponseEntity.status(201).body(createdRoute);
    }
}
