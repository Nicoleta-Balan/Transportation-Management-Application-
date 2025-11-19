package multitier.trans.service;

import multitier.trans.model.Reservation;
import multitier.trans.model.Route;
import multitier.trans.repository.ReservationRepository;
import multitier.trans.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service implementation for seat availability operations.
 * Replaces database functions calculate_booked_seats(), check_seat_availability(), get_available_seats().
 */
@Service
@Transactional(readOnly = true)
public class SeatAvailabilityServiceImpl implements SeatAvailabilityService {

    private final ReservationRepository reservationRepository;
    private final RouteRepository routeRepository;

    @Autowired
    public SeatAvailabilityServiceImpl(ReservationRepository reservationRepository,
                                      RouteRepository routeRepository) {
        this.reservationRepository = reservationRepository;
        this.routeRepository = routeRepository;
    }

    @Override
    public int calculateBookedSeats(Long routeId, LocalDateTime departureTime, LocalDateTime arrivalTime) {
        // Get all reservations for this route
        List<Reservation> reservations = reservationRepository.findByRouteId(routeId);

        // Filter reservations that overlap with the given time period
        // Two reservations overlap if: departure < res.arrival AND arrival > res.departure
        return reservations.stream()
                .filter(reservation -> {
                    // Only count confirmed or pending reservations
                    multitier.trans.model.enums.ReservationStatus status = reservation.getStatus();
                    if (status != multitier.trans.model.enums.ReservationStatus.CONFIRMED && 
                        status != multitier.trans.model.enums.ReservationStatus.PENDING) {
                        return false;
                    }

                    // Check for time overlap
                    LocalDateTime resDeparture = reservation.getTripDetails().getDepartureTime();
                    LocalDateTime resArrival = reservation.getTripDetails().getArrivalTime();

                    return departureTime.isBefore(resArrival) && arrivalTime.isAfter(resDeparture);
                })
                .mapToInt(Reservation::getSeatCount)
                .sum();
    }

    @Override
    public boolean checkSeatAvailability(Long routeId, int requestedSeats, 
                                         LocalDateTime departureTime, LocalDateTime arrivalTime) {
        int availableSeats = getAvailableSeats(routeId, departureTime, arrivalTime);
        return availableSeats >= requestedSeats;
    }

    @Override
    public int getAvailableSeats(Long routeId, LocalDateTime departureTime, LocalDateTime arrivalTime) {
        // Get route capacity
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found with id: " + routeId));

        int totalCapacity = route.getVehicleCapacity();
        int bookedSeats = calculateBookedSeats(routeId, departureTime, arrivalTime);
        
        return Math.max(0, totalCapacity - bookedSeats);
    }
}

