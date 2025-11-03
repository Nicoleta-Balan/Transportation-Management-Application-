package multitier.trans.service;

import multitier.trans.dto.CreateReservationRequest;
import multitier.trans.model.Reservation;
import multitier.trans.model.Route;
import multitier.trans.model.TripTimeDetails;
import multitier.trans.repository.ReservationRepository;
import multitier.trans.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation ("The Brain") for Reservation logic.
 * Implements the workflows for SCRUM-26 and SCRUM-27.
 */
@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final RouteRepository routeRepository; // We need this to find the Route

    @Autowired
    public ReservationServiceImpl(ReservationRepository reservationRepository, RouteRepository routeRepository) {
        this.reservationRepository = reservationRepository;
        this.routeRepository = routeRepository;
    }

    /* Reservation Creation */

    @Override
    public Reservation createReservation(CreateReservationRequest request) {
        // 1. Find the Route that the user wants to book
        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new RuntimeException("Route not found with id: " + request.getRouteId()));

        // 2. Create the Value Object
        TripTimeDetails tripDetails = new TripTimeDetails(
                request.getDepartureTime(),
                request.getArrivalTime()
        );

        // 3. Create the new Reservation entity
        Reservation newReservation = new Reservation();
        newReservation.setRoute(route);
        newReservation.setPassengerName(request.getPassengerName());
        newReservation.setSeatCount(request.getSeatCount());
        newReservation.setTripDetails(tripDetails);
        newReservation.setStatus("CONFIRMED"); // Business logic: New reservations are confirmed

        // 4. Save to database
        return reservationRepository.save(newReservation);
    }

    /*
     Reservation Cancellation
    */

    @Override
    public Reservation cancelReservation(Long reservationId) {
        // 1. Find the existing reservation
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + reservationId));

        // 2. Apply business logic
        // We could add logic here: e.g., "if (status == 'COMPLETED') { throw new Exception... }"
        reservation.setStatus("CANCELLED");

        // 3. Save the change
        return reservationRepository.save(reservation);
    }

    @Override
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    @Override
    public Optional<Reservation> getReservationById(Long id) {
        return reservationRepository.findById(id);
    }
}