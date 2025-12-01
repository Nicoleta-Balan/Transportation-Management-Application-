package multitier.trans.service;

import multitier.trans.dto.CreateReservationRequest;
import multitier.trans.dto.ReservationResponse;
import multitier.trans.model.Reservation;
import multitier.trans.model.Route;
import multitier.trans.model.TripTimeDetails;
import multitier.trans.repository.ReservationRepository;
import multitier.trans.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final RouteRepository routeRepository;

    @Autowired
    public ReservationServiceImpl(ReservationRepository reservationRepository, RouteRepository routeRepository) {
        this.reservationRepository = reservationRepository;
        this.routeRepository = routeRepository;
    }

    @Override
    public Reservation createReservation(CreateReservationRequest request) {
        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new RuntimeException("Route not found with id: " + request.getRouteId()));

        TripTimeDetails tripDetails = new TripTimeDetails(
                request.getDepartureTime(),
                request.getArrivalTime()
        );

        Reservation newReservation = new Reservation();
        newReservation.setRoute(route);
        newReservation.setPassengerName(request.getPassengerName());
        newReservation.setSeatCount(request.getSeatCount());
        newReservation.setTripDetails(tripDetails);
        newReservation.setStatus("CONFIRMED");
        
        // --- FIX: Transfer enum values from the request to the entity ---
        newReservation.setPassengerCategory(request.getPassengerCategory());
        newReservation.setVehicleClass(request.getVehicleClass());

        return reservationRepository.save(newReservation);
    }

    @Override
    public Reservation cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + reservationId));
        reservation.setStatus("CANCELLED");
        return reservationRepository.save(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getAllReservations() {
        List<Reservation> entities = reservationRepository.findAll();
        return entities.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReservationResponse> getReservationById(Long id) {
        return reservationRepository.findById(id)
                .map(this::mapToResponse);
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        Long routeId = null;
        String origin = "N/A";
        String destination = "N/A";
        LocalDateTime departure = null;
        LocalDateTime arrival = null;

        if (reservation.getRoute() != null) {
            routeId = reservation.getRoute().getId();
            if (reservation.getRoute().getOriginStation() != null) {
                origin = reservation.getRoute().getOriginStation().getName();
            }
            if (reservation.getRoute().getDestinationStation() != null) {
                destination = reservation.getRoute().getDestinationStation().getName();
            }
        }

        if (reservation.getTripDetails() != null) {
            departure = reservation.getTripDetails().getDepartureTime();
            arrival = reservation.getTripDetails().getArrivalTime();
        }

        return new ReservationResponse(
                reservation.getId(),
                reservation.getPassengerName(),
                reservation.getSeatCount(),
                reservation.getStatus(),
                routeId,
                origin,
                destination,
                departure,
                arrival
        );
    }
}