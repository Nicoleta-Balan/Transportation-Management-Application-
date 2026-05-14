package multitier.trans.service;

import multitier.trans.dto.CreateReservationRequest;
import multitier.trans.dto.ReservationResponse;
import multitier.trans.model.Reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationService {

    Reservation createReservation(CreateReservationRequest request);

    Reservation createReservationForUser(CreateReservationRequest request, String userEmail);

    Reservation cancelReservation(Long reservationId);

    List<Reservation> getAllReservations();

    Optional<Reservation> getReservationById(Long id);

    List<Reservation> findConfirmedReservationsForStation(Long stationId);

    List<Reservation> findPendingReservationsForStation(Long stationId);

    List<ReservationResponse> getActiveReservationsForUser(String userEmail);

    List<ReservationResponse> getPastReservationsForUser(String userEmail);
}