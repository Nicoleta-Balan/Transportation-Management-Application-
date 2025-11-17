package multitier.trans.controllers;

import jakarta.validation.Valid;
import multitier.trans.dto.CreateReservationRequest;
import multitier.trans.dto.FareCalculationResponse;
import multitier.trans.model.Reservation;
import multitier.trans.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
     * Gets all reservations (Admin only).
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Reservation> getAllReservations() {
        return reservationService.getAllReservations();
    }

    /**
     * GET /api/reservations/my-reservations
     * Gets reservations for the currently authenticated user.
     */
    @GetMapping("/my-reservations")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<Reservation> getMyReservations() {
        return reservationService.getMyReservations();
    }

    /**
     * GET /api/reservations/{id}
     * Gets a single reservation by its ID.
     * Users can only access their own reservations, admins can access any.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        return reservationService.getReservationById(id)
                .map(ResponseEntity::ok) // 200 OK if found
                .orElse(ResponseEntity.notFound().build()); // 404 Not Found if not
    }

    /**
     * GET /api/reservations/calculate-fare
     * Calculates the fare for a reservation before saving.
     * Used to display fare breakdown in the UI.
     * 
     * Query parameters:
     * - routeId: The route ID
     * - passengerCategory: ADULT, CHILD, SENIOR, STUDENT
     * - seatCount: Number of seats (1-10)
     * - departureTime: Departure date/time (ISO format: 2024-01-15T10:00:00)
     */
    @GetMapping("/calculate-fare")
    public ResponseEntity<FareCalculationResponse> calculateFare(
            @RequestParam Long routeId,
            @RequestParam String passengerCategory,
            @RequestParam Integer seatCount,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime departureTime) {
        
        FareCalculationResponse fare = reservationService.calculateFare(
                routeId,
                passengerCategory,
                seatCount,
                departureTime
        );
        
        return ResponseEntity.ok(fare);
    }
}