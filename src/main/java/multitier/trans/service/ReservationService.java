package multitier.trans.service;

import multitier.trans.dto.CreateReservationRequest;
import multitier.trans.dto.ReservationResponse;
import multitier.trans.model.Reservation;

import java.util.List;
import java.util.Optional;


public interface ReservationService {


    Reservation createReservation(CreateReservationRequest request);

    /**
     * Business logic for cancelling a reservation (SCRUM-27).
     */
    Reservation cancelReservation(Long reservationId);

    /* Finds all reservations. */
    List<ReservationResponse> getAllReservations();

    /**
     * Finds a single reservation by its ID.
     */

    Optional<ReservationResponse> getReservationById(Long id);
}