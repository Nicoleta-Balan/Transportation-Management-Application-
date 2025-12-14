package multitier.trans.controllers.rest;

import multitier.trans.dto.CreateTimetableRequest;
import multitier.trans.dto.UpdateTimetableRequest;
import multitier.trans.model.Timetable;
import multitier.trans.service.TimetableService;
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

import java.util.List;

@Tag(name = "Timetables", description = "API for managing transportation timetables")
@RepositoryRestController
@RequiredArgsConstructor
public class TimetableRestController {
    
    private final TimetableService timetableService;
    private final RepositoryEntityLinks entityLinks;

    @Operation(
        summary = "Create a new timetable",
        description = "Creates a new timetable for a route with the provided schedule information."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Timetable created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Timetable.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data or route has no stops"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Route not found"
        )
    })
    @PostMapping("/timetables")
    public ResponseEntity<EntityModel<Timetable>> createTimetable(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Timetable creation request",
                required = true,
                content = @Content(schema = @Schema(implementation = CreateTimetableRequest.class))
            )
            @Valid @RequestBody CreateTimetableRequest request) {
        
        Timetable created = timetableService.createTimetable(request);
        EntityModel<Timetable> resource = EntityModelUtils.createEntityModelSafe(
            created, Timetable.class, entityLinks);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(resource);
    }

    @Operation(
        summary = "Update a timetable",
        description = "Updates an existing timetable with the provided information."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Timetable updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Timetable.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Timetable not found"
        )
    })
    @PutMapping("/timetables/{id}")
    public ResponseEntity<EntityModel<Timetable>> updateTimetable(
            @Parameter(description = "Timetable ID", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Timetable update request",
                required = true,
                content = @Content(schema = @Schema(implementation = UpdateTimetableRequest.class))
            )
            @Valid @RequestBody UpdateTimetableRequest request) {
        
        Timetable updated = timetableService.updateTimetable(id, request);
        EntityModel<Timetable> resource = EntityModelUtils.createEntityModelSafe(
            updated, Timetable.class, entityLinks);
        
        return ResponseEntity.ok(resource);
    }

    @Operation(
        summary = "Delete a timetable",
        description = "Deletes a timetable by its ID."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Timetable deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Timetable not found"
        )
    })
    @DeleteMapping("/timetables/{id}")
    public ResponseEntity<Void> deleteTimetable(
            @Parameter(description = "Timetable ID", required = true, example = "1")
            @PathVariable Long id) {
        timetableService.deleteTimetable(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Get timetables by route ID",
        description = "Retrieves all timetables for a specific route."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved timetables",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Timetable.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Route not found"
        )
    })
    @GetMapping("/timetables/route/{routeId}")
    public ResponseEntity<List<Timetable>> getTimetablesByRouteId(
            @Parameter(description = "Route ID", required = true, example = "1")
            @PathVariable Long routeId) {
        
        List<Timetable> timetables = timetableService.findAllTimetablesByRouteId(routeId);
        // Return timetables directly (not wrapped in EntityModel) for compatibility with frontend
        // Same pattern as RouteRestController.getAllRoutes()
        return ResponseEntity.ok(timetables);
    }
}

