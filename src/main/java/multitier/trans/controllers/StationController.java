package multitier.trans.controllers;

import multitier.trans.dto.CreateStationRequest;
import multitier.trans.dto.UpdateStationRequest;
import multitier.trans.model.Station;
import multitier.trans.service.StationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.List;


@RestController
@RequestMapping("/api/stations-controller")
@Tag(name = "StationsController", description = "API endpoints for managing stations")
public class StationController {

    private final StationService stationService;

    @Autowired
    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @PostMapping
    @Operation(
            summary = "Create a new station",
            description = "Creates a new station with name, description, address, location, and status. All fields except description are required."
    )
    @RequestBody(
            description = "Station creation request",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateStationRequest.class),
                    examples = @ExampleObject(
                            name = "Example Station",
                            value = "{\"name\": \"Central Station\", \"description\": \"Main transportation hub\", \"address\": \"123 Main Street, City\", \"latitude\": 51.505, \"longitude\": -0.09, \"status\": \"ACTIVE\"}"
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Station created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Station.class),
                            examples = @ExampleObject(
                                    name = "Created Station",
                                    value = "{\"id\": 1, \"name\": \"Central Station\", \"description\": \"Main transportation hub\", \"address\": \"123 Main Street, City\", \"latitude\": 51.505, \"longitude\": -0.09, \"status\": \"ACTIVE\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or validation error (e.g., duplicate name or address)",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Station> createStation(@Valid @org.springframework.web.bind.annotation.RequestBody CreateStationRequest request) {
        Station createdStation = stationService.createStation(request);
        return ResponseEntity.status(201).body(createdStation); // 201 Created
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a station",
            description = "Updates an existing station. Only description, address, and status can be changed. Name cannot be modified."
    )
    @RequestBody(
            description = "Station update request",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UpdateStationRequest.class),
                    examples = @ExampleObject(
                            name = "Update Station",
                            value = "{\"description\": \"Updated description\", \"address\": \"456 New Street, City\", \"latitude\": 51.507, \"longitude\": -0.127, \"status\": \"ACTIVE\"}"
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Station updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Station.class),
                            examples = @ExampleObject(
                                    name = "Updated Station",
                                    value = "{\"id\": 1, \"name\": \"Central Station\", \"description\": \"Updated description\", \"address\": \"456 New Street, City\", \"latitude\": 51.507, \"longitude\": -0.127, \"status\": \"ACTIVE\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or duplicate address",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Station not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Station> updateStation(
            @Parameter(
                    name = "id",
                    description = "Unique identifier of the station to update",
                    required = true,
                    in = ParameterIn.PATH,
                    schema = @Schema(type = "integer", format = "int64", example = "1")
            )
            @PathVariable Long id,
            @Valid @org.springframework.web.bind.annotation.RequestBody UpdateStationRequest request) {
        try {
            Station updatedStation = stationService.updateStation(id, request);
            return ResponseEntity.ok(updatedStation); // 200 OK
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @Operation(
            summary = "Get all stations",
            description = "Retrieves a list of all stations in the system."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of stations retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Station.class, type = "array"),
                            examples = @ExampleObject(
                                    name = "Stations List",
                                    value = "[{\"id\": 1, \"name\": \"Central Station\", \"description\": \"Main hub\", \"address\": \"123 Main St\", \"latitude\": 51.505, \"longitude\": -0.09, \"status\": \"ACTIVE\"}, {\"id\": 2, \"name\": \"North Station\", \"description\": \"North terminal\", \"address\": \"456 North Ave\", \"latitude\": 51.515, \"longitude\": -0.10, \"status\": \"ACTIVE\"}]"
                            )
                    )
            )
    })
    public List<Station> getAllStations() {
        return stationService.getAllStations();
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a station",
            description = "Deletes an existing station by ID. Returns 204 No Content on success."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Station deleted successfully",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Station cannot be deleted (e.g., in use by routes)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error Response",
                                    value = "{\"message\": \"Station cannot be deleted because it is in use by routes\", \"error\": \"Station cannot be deleted because it is in use by routes\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Station not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Error Response",
                                    value = "{\"message\": \"Station not found\", \"error\": \"Station not found\"}"
                            )
                    )
            )
    })
    public ResponseEntity<?> deleteStation(
            @Parameter(
                    name = "id",
                    description = "Unique identifier of the station to delete",
                    required = true,
                    in = ParameterIn.PATH,
                    schema = @Schema(type = "integer", format = "int64", example = "1")
            )
            @PathVariable Long id) {
        try {
            stationService.deleteStation(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            if (e.getMessage().contains("not found")) {
                errorResponse.put("message", "Station not found");
                errorResponse.put("error", "Station not found");
                return ResponseEntity.status(404).body(errorResponse);
            }
            errorResponse.put("message", e.getMessage());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(400).body(errorResponse);
        }
    }
}