package multitier.trans.controllers.rest;

import multitier.trans.dto.CreateRouteRequest;
import multitier.trans.dto.UpdateRouteRequest;
import multitier.trans.model.Route;
import multitier.trans.service.RouteService;
import java.util.List;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import multitier.trans.utils.EntityModelUtils;


@Tag(name = "Routes", description = "API for managing transportation routes")
@RepositoryRestController
@RequiredArgsConstructor
public class RouteRestController {
    
    private final RouteService routeService;
    private final RepositoryEntityLinks entityLinks;
    
    @Operation(
        summary = "Get all routes with stops",
        description = "Returns all routes with their stops eagerly loaded."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Routes retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Route.class))
        )
    })
    @GetMapping("/routes/search/allWithStops")
    public ResponseEntity<List<Route>> getAllRoutesWithStops() {
        List<Route> routes = routeService.findAllRoutes();
        return ResponseEntity.ok(routes);
    }
    
    @Operation(
        summary = "Get route by ID with stops",
        description = "Returns a route by ID with its stops eagerly loaded."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Route retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Route.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Route not found"
        )
    })
    @GetMapping("/routes/search/findByIdWithStops")
    public ResponseEntity<Route> getRouteByIdWithStops(
            @Parameter(description = "Route ID", required = true, example = "1")
            @RequestParam Long id) {
        Route route = routeService.getRouteById(id);
        return ResponseEntity.ok(route);
    }
    
    @Operation(
        summary = "Create a new route",
        description = "Creates a new route with the provided information including stops."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Route created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Route.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data"
        )
    })
    @PostMapping("/routes")
    public ResponseEntity<EntityModel<Route>> createRoute(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Route creation request",
                required = true,
                content = @Content(schema = @Schema(implementation = CreateRouteRequest.class))
            )
            @Valid @RequestBody CreateRouteRequest request) {
        
        Route created = routeService.createRoute(request);
        
        // The service method already reloads the route with stops eagerly loaded
        // and initializes the collection within its transaction
        // The collection should be initialized and ready for serialization
        EntityModel<Route> resource = EntityModelUtils.createEntityModel(
            created, Route.class, entityLinks);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(resource);
    }
    
    @Operation(
        summary = "Update a route",
        description = "Updates an existing route with the provided information."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Route updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Route.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Route not found"
        )
    })
    @PutMapping("/routes/{id}")
    public ResponseEntity<EntityModel<Route>> updateRoute(
            @Parameter(description = "Route ID", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Route update request",
                required = true,
                content = @Content(schema = @Schema(implementation = UpdateRouteRequest.class))
            )
            @Valid @RequestBody UpdateRouteRequest request) {
        
        Route updated = routeService.updateRoute(id, request);
        EntityModel<Route> resource = EntityModelUtils.createEntityModel(
            updated, Route.class, entityLinks);
        
        return ResponseEntity.ok(resource);
    }
    
    @Operation(
        summary = "Delete a route",
        description = "Deletes a route by its ID."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Route deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Route not found"
        )
    })
    @DeleteMapping("/routes/{id}")
    public ResponseEntity<Void> deleteRoute(
            @Parameter(description = "Route ID", required = true, example = "1")
            @PathVariable Long id) {
        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }
}

