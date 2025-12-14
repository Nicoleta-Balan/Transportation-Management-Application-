package multitier.trans.service;

import multitier.trans.dto.CreateReservationRequest;
import multitier.trans.factory.ReservationFactory;
import multitier.trans.model.Reservation;
import multitier.trans.model.Route;
import multitier.trans.model.RouteStop;
import multitier.trans.model.enums.ReservationStatus;
import multitier.trans.model.enums.StationStatus;
import multitier.trans.repository.ReservationRepository;
import multitier.trans.repository.RouteRepository;
import multitier.trans.utils.RepositoryUtils;
import multitier.trans.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor  // Lombok: Generates constructor for final fields
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationFactory reservationFactory;
    private final RouteRepository routeRepository;

    @Override
    @Transactional
    public Reservation createReservation(CreateReservationRequest request) {
        // Load route with stops to validate station statuses
        Route route = RepositoryUtils.findByIdOrThrow(
            routeRepository.findByIdWithStops(request.getRouteId()),
            "Route",
            request.getRouteId()
        );

        // Validate all stations in route are active
        List<RouteStop> inactiveStops = route.getRouteStops().stream()
            .filter(stop -> stop.getStation().getStatus() != StationStatus.ACTIVE)
            .collect(Collectors.toList());

        if (!inactiveStops.isEmpty()) {
            String stationNames = inactiveStops.stream()
                .map(stop -> stop.getStation().getName())
                .collect(Collectors.joining(", "));

            throw new BusinessException(
                String.format("Cannot create reservation: route contains inactive or maintenance stations: %s", 
                    stationNames)
            );
        }

        // All stations are active, proceed with reservation creation
        Reservation reservation = reservationFactory.createReservation(request);
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation cancelReservation(Long reservationId) {
        Reservation reservation = RepositoryUtils.findByIdOrThrow(
            reservationRepository.findById(reservationId),
            "Reservation",
            reservationId
        );
        reservation.setStatus(ReservationStatus.CANCELLED);
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

    @Override
    public List<Reservation> findConfirmedReservationsForStation(Long stationId) {
        return reservationRepository.findByStationIdAndStatusIn(
            stationId,
            List.of(ReservationStatus.CONFIRMED)
        );
    }

    @Override
    public List<Reservation> findPendingReservationsForStation(Long stationId) {
        return reservationRepository.findByStationIdAndStatusIn(
            stationId,
            List.of(ReservationStatus.PENDING)
        );
    }
}