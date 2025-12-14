package multitier.trans.controllers.rest;

import multitier.trans.dto.CreateStationRequest;
import multitier.trans.dto.UpdateStationRequest;
import multitier.trans.model.Station;
import multitier.trans.service.StationService;
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

@Tag(name = "Stations", description = "API for managing transportation stations")
@RepositoryRestController
@RequiredArgsConstructor
public class StationRestController {
    
    private final StationService stationService;
    private final RepositoryEntityLinks entityLinks;
    
    @Operation(
        summary = "Create a new station",
        description = "Creates a new station with the provided information."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Station created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Station.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data"
        )
    })
    @PostMapping("/stations")
    public ResponseEntity<EntityModel<Station>> createStation(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Station creation request",
                required = true,
                content = @Content(schema = @Schema(implementation = CreateStationRequest.class))
            )
            @Valid @RequestBody CreateStationRequest request) {
        
        Station created = stationService.createStation(request);
        EntityModel<Station> resource = EntityModelUtils.createEntityModel(
            created, Station.class, entityLinks);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(resource);
    }
    
    @Operation(
        summary = "Update a station",
        description = "Updates an existing station with the provided information."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Station updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Station.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Station not found"
        )
    })
    @PutMapping("/stations/{id}")
    public ResponseEntity<EntityModel<Station>> updateStation(
            @Parameter(description = "Station ID", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Station update request",
                required = true,
                content = @Content(schema = @Schema(implementation = UpdateStationRequest.class))
            )
            @Valid @RequestBody UpdateStationRequest request) {
        
        Station updated = stationService.updateStation(id, request);
        EntityModel<Station> resource = EntityModelUtils.createEntityModel(
            updated, Station.class, entityLinks);
        
        return ResponseEntity.ok(resource);
    }
    
    @Operation(
        summary = "Delete a station",
        description = "Deletes a station by its ID. Cannot delete stations that are used in routes or have existing reservations."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Station deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Station not found"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Cannot delete station (in use)"
        )
    })
    @DeleteMapping("/stations/{id}")
    public ResponseEntity<Void> deleteStation(
            @Parameter(description = "Station ID", required = true, example = "1")
            @PathVariable Long id) {
        stationService.deleteStation(id);
        return ResponseEntity.noContent().build();
    }
}

