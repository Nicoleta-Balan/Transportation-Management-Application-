package multitier.trans.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import multitier.trans.dto.seat.OccupiedSeatsResponse;
import multitier.trans.dto.seat.SeatHoldRequest;
import multitier.trans.dto.seat.SeatHoldResponse;
import multitier.trans.exception.BusinessException;
import multitier.trans.model.Reservation;
import multitier.trans.model.Route;
import multitier.trans.model.SeatHold;
import multitier.trans.model.User;
import multitier.trans.model.enums.ReservationStatus;
import multitier.trans.repository.ReservationRepository;
import multitier.trans.repository.RouteRepository;
import multitier.trans.repository.SeatHoldRepository;
import multitier.trans.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatService {

    private final SeatHoldRepository seatHoldRepository;
    private final ReservationRepository reservationRepository;
    private final RouteRepository routeRepository;
    private final UserRepository userRepository;

    private static final int HOLD_DURATION_MINUTES = 15;

    /**
     * Get all occupied and held seats for a route/departure
     */
    @Transactional(readOnly = true)
    public OccupiedSeatsResponse getOccupiedSeats(Long routeId, LocalDateTime departureTime, String sessionId) {
        LocalDateTime now = LocalDateTime.now();

        // Use a time window to find reservations (handles precision differences)
        LocalDateTime startTime = departureTime.withSecond(0).withNano(0);
        LocalDateTime endTime = startTime.plusMinutes(1);

        // Get confirmed reservation seats using time range
        List<Reservation> confirmedReservations = reservationRepository.findByRouteIdAndDepartureTimeRangeAndStatusIn(
            routeId,
            startTime,
            endTime,
            Arrays.asList(ReservationStatus.PENDING, ReservationStatus.CONFIRMED)
        );

        List<String> occupiedSeats = confirmedReservations.stream()
            .filter(r -> r.getSelectedSeats() != null && !r.getSelectedSeats().isEmpty())
            .flatMap(r -> Arrays.stream(r.getSelectedSeats().split(",")))
            .map(String::trim)
            .collect(Collectors.toList());

        // Get all held seats using time range
        List<String> allHeldSeats = seatHoldRepository.findHeldSeatNumbersByTimeRange(routeId, startTime, endTime, now);

        // Get seats held by current session
        List<SeatHold> myHolds = seatHoldRepository.findActiveHoldsBySession(sessionId, now);
        List<String> myHeldSeats = myHolds.stream()
            .filter(h -> h.getRoute().getId().equals(routeId) && h.getDepartureTime().equals(departureTime))
            .map(SeatHold::getSeatNumber)
            .collect(Collectors.toList());

        // Other users' held seats (exclude my holds)
        List<String> otherHeldSeats = allHeldSeats.stream()
            .filter(seat -> !myHeldSeats.contains(seat))
            .collect(Collectors.toList());

        return OccupiedSeatsResponse.builder()
            .routeId(routeId)
            .departureTime(departureTime.toString())
            .occupiedSeats(occupiedSeats)
            .heldSeats(otherHeldSeats)
            .myHeldSeats(myHeldSeats)
            .build();
    }

    /**
     * Hold seats for a user during booking process
     */
    @Transactional
    public SeatHoldResponse holdSeats(SeatHoldRequest request, String userEmail) {
        Route route = routeRepository.findById(request.getRouteId())
            .orElseThrow(() -> new BusinessException("Route not found"));

        User user = null;
        if (userEmail != null) {
            user = userRepository.findByEmail(userEmail).orElse(null);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(HOLD_DURATION_MINUTES);

        List<String> heldSeats = new ArrayList<>();
        List<String> failedSeats = new ArrayList<>();

        // First, release any existing holds for this session on this route/departure
        List<SeatHold> existingHolds = seatHoldRepository.findActiveHoldsBySession(request.getSessionId(), now)
            .stream()
            .filter(h -> h.getRoute().getId().equals(request.getRouteId())
                && h.getDepartureTime().equals(request.getDepartureTime()))
            .collect(Collectors.toList());
        seatHoldRepository.deleteAll(existingHolds);

        // Use time range to find reservations (handles precision differences)
        LocalDateTime startTime = request.getDepartureTime().withSecond(0).withNano(0);
        LocalDateTime endTime = startTime.plusMinutes(1);

        // Check occupied seats from confirmed reservations using time range
        List<Reservation> confirmedReservations = reservationRepository.findByRouteIdAndDepartureTimeRangeAndStatusIn(
            request.getRouteId(),
            startTime,
            endTime,
            Arrays.asList(ReservationStatus.PENDING, ReservationStatus.CONFIRMED)
        );

        List<String> occupiedSeats = confirmedReservations.stream()
            .filter(r -> r.getSelectedSeats() != null && !r.getSelectedSeats().isEmpty())
            .flatMap(r -> Arrays.stream(r.getSelectedSeats().split(",")))
            .map(String::trim)
            .collect(Collectors.toList());

        // Also check seats held by other users using time range
        List<String> otherHeldSeats = seatHoldRepository.findHeldSeatNumbersByTimeRange(request.getRouteId(), startTime, endTime, now);
        
        // Combine occupied and held seats
        List<String> unavailableSeats = new ArrayList<>(occupiedSeats);
        unavailableSeats.addAll(otherHeldSeats);

        List<String> requestedSeats = request.getSeatNumbers();
        
        // If no specific seats requested, allocate automatically
        if (requestedSeats == null || requestedSeats.isEmpty()) {
            int seatCount = request.getSeatCount() > 0 ? request.getSeatCount() : 1;
            requestedSeats = allocateSeats(route, unavailableSeats, seatCount);
        }

        for (String seatNumber : requestedSeats) {
            // Check if seat is already booked or held
            if (unavailableSeats.contains(seatNumber)) {
                failedSeats.add(seatNumber);
                continue;
            }

            // Create new hold
            SeatHold hold = SeatHold.builder()
                .route(route)
                .departureTime(request.getDepartureTime())
                .seatNumber(seatNumber)
                .sessionId(request.getSessionId())
                .user(user)
                .createdAt(now)
                .expiresAt(expiresAt)
                .build();

            seatHoldRepository.save(hold);
            heldSeats.add(seatNumber);
        }

        boolean success = !heldSeats.isEmpty();
        String message = failedSeats.isEmpty()
            ? "All seats held successfully"
            : String.format("Some seats unavailable: %s", String.join(", ", failedSeats));

        return SeatHoldResponse.builder()
            .success(success)
            .message(message)
            .heldSeats(heldSeats)
            .failedSeats(failedSeats)
            .expiresAt(expiresAt)
            .build();
    }

    private List<String> allocateSeats(Route route, List<String> occupiedSeats, int count) {
        List<String> allocated = new ArrayList<>();
        int capacity = route.getVehicleCapacity();
        
        // Generate all possible seats
        List<String> allSeats = new ArrayList<>();
        for (int i = 1; i <= capacity; i++) {
            allSeats.add(String.valueOf(i));
        }
        
        // Filter out occupied seats
        List<String> availableSeats = allSeats.stream()
            .filter(seat -> !occupiedSeats.contains(seat))
            .collect(Collectors.toList());
            
        if (availableSeats.size() < count) {
            throw new BusinessException("Not enough seats available");
        }
        
        if (count == 1) {
            // Random allocation for single seat
            Random random = new Random();
            int index = random.nextInt(availableSeats.size());
            allocated.add(availableSeats.get(index));
        } else {
            // Try to find adjacent seats for multiple passengers
            // Assuming seats are numbered sequentially
            boolean found = false;
            for (int i = 0; i <= availableSeats.size() - count; i++) {
                boolean sequence = true;
                int startSeat = Integer.parseInt(availableSeats.get(i));
                
                for (int j = 1; j < count; j++) {
                    int nextSeat = Integer.parseInt(availableSeats.get(i + j));
                    if (nextSeat != startSeat + j) {
                        sequence = false;
                        break;
                    }
                }
                
                if (sequence) {
                    for (int j = 0; j < count; j++) {
                        allocated.add(availableSeats.get(i + j));
                    }
                    found = true;
                    break;
                }
            }
            
            // If no adjacent seats found, just pick random ones
            if (!found) {
                Random random = new Random();
                for (int i = 0; i < count; i++) {
                    int index = random.nextInt(availableSeats.size());
                    allocated.add(availableSeats.get(index));
                    availableSeats.remove(index);
                }
            }
        }
        
        return allocated;
    }

    /**
     * Release seat holds for a session
     */
    @Transactional
    public void releaseHolds(String sessionId) {
        seatHoldRepository.deleteBySessionId(sessionId);
    }

    /**
     * Scheduled task to clean up expired holds (runs every minute)
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredHolds() {
        int deleted = seatHoldRepository.deleteExpiredHolds(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Cleaned up {} expired seat holds", deleted);
        }
    }
}
