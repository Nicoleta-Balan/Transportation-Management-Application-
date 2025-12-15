package multitier.trans.controllers.rest;

import multitier.trans.dto.CreateReservationRequest;
import multitier.trans.model.Reservation;
import multitier.trans.service.ReservationService;
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

@Tag(name = "Reservations", description = "API for managing transportation reservations")
@RepositoryRestController
@RequiredArgsConstructor
public class ReservationRestController {
    
    private final ReservationService reservationService;
    private final RepositoryEntityLinks entityLinks;
    
    @Operation(
        summary = "Create a new reservation",
        description = "Creates a new reservation for a route with passenger and trip details."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Reservation created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Reservation.class))
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
    @PostMapping("/reservations")
    public ResponseEntity<EntityModel<Reservation>> createReservation(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Reservation creation request",
                required = true,
                content = @Content(schema = @Schema(implementation = CreateReservationRequest.class))
            )
            @Valid @RequestBody CreateReservationRequest request) {
        
        Reservation created = reservationService.createReservation(request);
        EntityModel<Reservation> resource = EntityModelUtils.createEntityModelSafe(
            created, Reservation.class, entityLinks);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(resource);
    }
    
    @Operation(
        summary = "Cancel a reservation",
        description = "Cancels an existing reservation by setting its status to CANCELLED."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Reservation cancelled successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Reservation.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Reservation not found"
        )
    })
    @PutMapping("/reservations/{id}/cancel")
    public ResponseEntity<EntityModel<Reservation>> cancelReservation(
            @Parameter(description = "Reservation ID", required = true, example = "1")
            @PathVariable Long id) {
        Reservation cancelled = reservationService.cancelReservation(id);
        EntityModel<Reservation> resource = EntityModelUtils.createEntityModelSafe(
            cancelled, Reservation.class, entityLinks);
        
        return ResponseEntity.ok(resource);
    }
    
    @Operation(
        summary = "Update reservation (not allowed)",
        description = "Standard update is not allowed for reservations. Use /cancel endpoint instead.",
        hidden = true
    )
    @ApiResponse(responseCode = "405", description = "Method not allowed")
    @PutMapping("/reservations/{id}")
    public ResponseEntity<Void> updateReservation(
            @Parameter(description = "Reservation ID", required = true)
            @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }
    
    @Operation(
        summary = "Delete reservation (not allowed)",
        description = "Deletion is not allowed for reservations. Use /cancel endpoint instead.",
        hidden = true
    )
    @ApiResponse(responseCode = "405", description = "Method not allowed")
    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> deleteReservation(
            @Parameter(description = "Reservation ID", required = true)
            @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }
}

