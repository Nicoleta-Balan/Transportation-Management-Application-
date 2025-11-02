package multitier.trans.controllers;

import jakarta.validation.Valid;
import multitier.trans.dto.CreateReservationRequest;
import multitier.trans.model.Reservation;
import multitier.trans.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API Controller ("The Gate") for handling all Reservation requests.
 */

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @Autowired
    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * Endpoint for: Reservation Creation
     * POST /api/reservations
     */

    @PostMapping
    public ResponseEntity<Reservation> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        Reservation newReservation = reservationService.createReservation(request);
        return new ResponseEntity<>(newReservation, HttpStatus.CREATED); // 201 Created
    }

    /**
     * Reservation Cancellation
     * PUT /api/reservations/{id}/cancel
     * (We use PUT or PATCH for updates)
     */

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Reservation> cancelReservation(@PathVariable Long id) {
        Reservation cancelledReservation = reservationService.cancelReservation(id);
        return ResponseEntity.ok(cancelledReservation); // 200 OK
    }

    /**
     * GET /api/reservations
     * Gets all reservations.
     */

    @GetMapping
    public List<Reservation> getAllReservations() {
        return reservationService.getAllReservations();
    }

    /**
     * GET /api/reservations/{id}
     * Gets a single reservation by its ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        return reservationService.getReservationById(id)
                .map(ResponseEntity::ok) // 200 OK if found
                .orElse(ResponseEntity.notFound().build()); // 404 Not Found if not
    }
}