package multitier.trans.service;

import multitier.trans.dto.CreateReservationRequest;
import multitier.trans.dto.ReservationResponse;
import multitier.trans.factory.ReservationFactory;
import multitier.trans.model.Reservation;
import multitier.trans.model.Route;
import multitier.trans.model.RouteStop;
import multitier.trans.model.User;
import multitier.trans.model.enums.ReservationStatus;
import multitier.trans.model.enums.StationStatus;
import multitier.trans.model.enums.VehicleClass;
import multitier.trans.repository.ReservationRepository;
import multitier.trans.repository.RouteRepository;
import multitier.trans.repository.SeatHoldRepository;
import multitier.trans.repository.UserRepository;
import multitier.trans.utils.RepositoryUtils;
import multitier.trans.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor  // Lombok: Generates constructor for final fields
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationFactory reservationFactory;
    private final RouteRepository routeRepository;
    private final UserRepository userRepository;
    private final SeatHoldRepository seatHoldRepository;

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

        // Auto-allocate seats if none selected
        if (request.getSelectedSeats() == null || request.getSelectedSeats().isEmpty()) {
            List<String> allocatedSeats = autoAllocateSeats(route, request.getDepartureTime(), request.getSeatCount());
            request.setSelectedSeats(allocatedSeats);
        }

        // All stations are active, proceed with reservation creation
        Reservation reservation = reservationFactory.createReservation(request);

        // If sessionId is provided, release the holds for this session
        if (request.getSessionId() != null) {
            seatHoldRepository.deleteBySessionId(request.getSessionId());
        }

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

    @Override
    @Transactional
    public Reservation createReservationForUser(CreateReservationRequest request, String userEmail) {
        // We can't just call createReservation(request) because we need to set the user BEFORE saving
        // to ensure any JPA auditing or listeners have the correct state, and to avoid an extra update.
        
        // 1. Load route and validate (same logic as createReservation)
        Route route = RepositoryUtils.findByIdOrThrow(
            routeRepository.findByIdWithStops(request.getRouteId()),
            "Route",
            request.getRouteId()
        );

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

        // 2. Auto-allocate seats if none selected
        if (request.getSelectedSeats() == null || request.getSelectedSeats().isEmpty()) {
            List<String> allocatedSeats = autoAllocateSeats(route, request.getDepartureTime(), request.getSeatCount());
            request.setSelectedSeats(allocatedSeats);
        }

        // 3. Create reservation entity
        Reservation reservation = reservationFactory.createReservation(request);

        // 5. Attach user if found
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user != null) {
                reservation.setUser(user);
            }
        }

        // 6. Release holds if sessionId provided
        if (request.getSessionId() != null) {
            seatHoldRepository.deleteBySessionId(request.getSessionId());
        }

        // 7. Save
        return reservationRepository.save(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getActiveReservationsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new BusinessException("User not found"));

        List<Reservation> reservations = reservationRepository.findActiveReservationsByUserId(
            user.getId(),
            Arrays.asList(ReservationStatus.PENDING, ReservationStatus.CONFIRMED),
            LocalDateTime.now()
        );

        return reservations.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getPastReservationsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new BusinessException("User not found"));

        List<Reservation> reservations = reservationRepository.findPastReservationsByUserId(
            user.getId(),
            LocalDateTime.now()
        );

        return reservations.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Auto-allocate seats when none are selected
     */
    private List<String> autoAllocateSeats(Route route, LocalDateTime departureTime, int seatCount) {
        // Get already occupied seats for this route/departure
        List<Reservation> existingReservations = reservationRepository.findByRouteIdAndDepartureTimeAndStatusIn(
            route.getId(),
            departureTime,
            Arrays.asList(ReservationStatus.PENDING, ReservationStatus.CONFIRMED)
        );

        List<String> occupiedSeats = existingReservations.stream()
            .filter(r -> r.getSelectedSeats() != null && !r.getSelectedSeats().isEmpty())
            .flatMap(r -> Arrays.stream(r.getSelectedSeats().split(",")))
            .map(String::trim)
            .collect(Collectors.toList());

        // Generate all possible seats based on vehicle capacity
        List<String> allSeats = new ArrayList<>();
        
        if (route.getVehicleClass() == VehicleClass.DOUBLE_DECKER) {
            // Double Decker Logic: L1A-L10D and U1A-U10D
            // Lower Deck: 10 rows * 4 seats = 40 seats
            for (int row = 1; row <= 10; row++) {
                for (String col : new String[]{"A", "B", "C", "D"}) {
                    allSeats.add("L" + row + col);
                }
            }
            // Upper Deck: 10 rows * 4 seats = 40 seats
            for (int row = 1; row <= 10; row++) {
                for (String col : new String[]{"A", "B", "C", "D"}) {
                    allSeats.add("U" + row + col);
                }
            }
        } else {
            // Standard Bus Logic: 1A, 1B, 1C, 1D, etc.
            int capacity = route.getVehicleCapacity();
            int rows = (int) Math.ceil((double) capacity / 4);
            
            for (int row = 1; row <= rows; row++) {
                for (String col : new String[]{"A", "B", "C", "D"}) {
                    if (allSeats.size() < capacity) {
                        allSeats.add(row + col);
                    }
                }
            }
        }

        // Filter out occupied seats
        List<String> availableSeats = allSeats.stream()
            .filter(seat -> !occupiedSeats.contains(seat))
            .collect(Collectors.toList());

        if (availableSeats.size() < seatCount) {
            throw new BusinessException("Not enough seats available. Only " + availableSeats.size() + " seats left.");
        }

        List<String> allocated = new ArrayList<>();

        if (seatCount == 1) {
            // Random allocation for single seat
            Random random = new Random();
            int index = random.nextInt(availableSeats.size());
            allocated.add(availableSeats.get(index));
        } else {
            // Try to find adjacent seats for multiple passengers
            // This is simplified for alphanumeric seats - just picking from available list
            // A better algorithm would check for same row
            
            // Simple strategy: take first N available seats
            // Since allSeats is generated in order (row by row), this tends to keep people together
            for (int i = 0; i < seatCount; i++) {
                allocated.add(availableSeats.get(i));
            }
        }

        return allocated;
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        return ReservationResponse.builder()
            .id(reservation.getId())
            .routeId(reservation.getRoute().getId())
            .originStation(reservation.getRoute().getOriginStation().getName())
            .destinationStation(reservation.getRoute().getDestinationStation().getName())
            .passengerName(reservation.getPassengerName())
            .seatCount(reservation.getSeatCount())
            .selectedSeats(reservation.getSelectedSeats())
            .status(reservation.getStatus())
            .departureTime(reservation.getTripDetails() != null ? reservation.getTripDetails().getDepartureTime() : null)
            .arrivalTime(reservation.getTripDetails() != null ? reservation.getTripDetails().getArrivalTime() : null)
            .passengerCategory(reservation.getPassengerCategory())
            .vehicleClass(reservation.getVehicleClass())
            .totalPrice(reservation.getTotalPrice())
            .currency(reservation.getCurrency())
            .createdAt(reservation.getCreatedAt())
            .qrCode(UUID.randomUUID().toString())
            .build();
    }
}