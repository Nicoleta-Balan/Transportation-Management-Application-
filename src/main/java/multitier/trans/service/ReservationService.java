package multitier.trans.service;

import multitier.trans.dto.CreateReservationRequest;
import multitier.trans.dto.FareCalculationResponse;
import multitier.trans.model.Reservation;

import java.util.List;
import java.util.Optional;

/* Service Layer Interface ("The Menu") for Reservation business logic. */

public interface ReservationService {


    Reservation createReservation(CreateReservationRequest request);

    /**
     * Business logic for cancelling a reservation (SCRUM-27).
     */
    Reservation cancelReservation(Long reservationId);

    /* Finds all reservations (Admin only). */
    List<Reservation> getAllReservations();

    /**
     * Finds all reservations for the currently authenticated user.
     */
    List<Reservation> getMyReservations();

    /**
     * Finds a single reservation by its ID.
     * Users can only access their own reservations, admins can access any.
     */
    Optional<Reservation> getReservationById(Long id);

    /**
     * Calculates the fare for a reservation before saving.
     * Used to display fare breakdown in the UI.
     */
    FareCalculationResponse calculateFare(Long routeId, String passengerCategory, Integer seatCount, java.time.LocalDateTime departureTime);
}