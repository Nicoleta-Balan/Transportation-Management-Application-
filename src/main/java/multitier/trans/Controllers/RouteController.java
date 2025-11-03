package multitier.trans.Controllers;

import multitier.trans.model.Route;
import multitier.trans.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST Controller to handle HTTP requests for managing routes (the API endpoints).
 * This controller exposes the 'RouteRepository' functionality to the web.
 */
@RestController
@RequestMapping("/api/routes") // All endpoints start with /api/routes
public class RouteController {

    // Spring automatically inserts the Repository instance here (Dependency Injection)
    private final RouteRepository routeRepository;

    @Autowired
    // We use the @Autowired constructor for dependency injection (best practice)
    public RouteController(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    /**
     * GET endpoint (Read): Fetches all routes from the PostgreSQL database.
     * Completes the 'Read' part of your TMS-1 CRUD story.
     * Accessible via GET http://localhost:8080/api/routes
     */
    @GetMapping
    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    /**
     * POST endpoint (Create): Saves a new route record to the PostgreSQL database.
     * Completes the 'Create' part of your TMS-1 CRUD story.
     * Accessible via POST http://localhost:8080/api/routes
     */
    @PostMapping
    public Route createRoute(@RequestBody Route route) {
        // The save() method is inherited from JpaRepository
        return routeRepository.save(route);
    }
}
