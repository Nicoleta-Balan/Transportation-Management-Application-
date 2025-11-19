package multitier.trans.service;

import multitier.trans.dto.CreateReservationRequest;
import multitier.trans.dto.FareCalculationResponse;
import multitier.trans.factory.ReservationFactory;
import multitier.trans.model.Reservation;
import multitier.trans.model.Route;
import multitier.trans.model.User;
import multitier.trans.model.enums.PassengerCategory;
import multitier.trans.model.enums.ReservationStatus;
import multitier.trans.model.enums.VehicleClass;
import multitier.trans.model.ReservationStatusHistory;
import multitier.trans.repository.ReservationRepository;
import multitier.trans.repository.ReservationStatusHistoryRepository;
import multitier.trans.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for Reservation logic.
 * Uses Spring Data JPA repositories for all data access operations.
 * Uses ReservationFactory for creating reservation entities.
 */

@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final RouteRepository routeRepository;
    private final multitier.trans.service.UserService userService;
    private final ReservationFactory reservationFactory;
    private final FareCalculationService fareCalculationService;
    private final SeatAvailabilityService seatAvailabilityService;
    private final ReservationStatusHistoryRepository reservationStatusHistoryRepository;

    @Autowired
    public ReservationServiceImpl(
            ReservationRepository reservationRepository,
            RouteRepository routeRepository,
            multitier.trans.service.UserService userService,
            ReservationFactory reservationFactory,
            FareCalculationService fareCalculationService,
            SeatAvailabilityService seatAvailabilityService,
            ReservationStatusHistoryRepository reservationStatusHistoryRepository) {
        this.reservationRepository = reservationRepository;
        this.routeRepository = routeRepository;
        this.userService = userService;
        this.reservationFactory = reservationFactory;
        this.fareCalculationService = fareCalculationService;
        this.seatAvailabilityService = seatAvailabilityService;
        this.reservationStatusHistoryRepository = reservationStatusHistoryRepository;
    }

    /**
     * Reservation Creation
     * Automatically associates the reservation with the currently authenticated user.
     * Uses ReservationFactory to encapsulate creation logic.
     */
    @Override
    public Reservation createReservation(CreateReservationRequest request) {
        // 1. Get the currently authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);

        // 2. Find the Route
        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new RuntimeException("Route not found with id: " + request.getRouteId()));

        // 3. Business rule validation: Check seat availability
        // This replaces database trigger validate_seat_capacity()
        if (!seatAvailabilityService.checkSeatAvailability(
                route.getId(),
                request.getSeatCount(),
                request.getDepartureTime(),
                request.getArrivalTime())) {
            throw new RuntimeException("Insufficient seats available for the requested time period");
        }

        // 4. Business rule validation: Validate time constraints
        // This replaces database trigger validate_reservation_times()
        if (request.getArrivalTime().isBefore(request.getDepartureTime()) || 
            request.getArrivalTime().equals(request.getDepartureTime())) {
            throw new RuntimeException("Arrival time must be after departure time");
        }

        // Check if departure time is not too far in the past
        if (request.getDepartureTime().isBefore(LocalDateTime.now().minusDays(1))) {
            throw new RuntimeException("Departure time cannot be more than 1 day in the past");
        }

        // Check if departure time is not too far in the future
        if (request.getDepartureTime().isAfter(LocalDateTime.now().plusYears(1))) {
            throw new RuntimeException("Departure time cannot be more than 1 year in the future");
        }

        // 5. Use Factory to create Reservation
        // Factory handles: passenger details defaults, TripTimeDetails creation, status initialization
        Reservation newReservation = reservationFactory.createReservation(user, route, request);

        // 6. Calculate and set denormalized fields (replaces database trigger)
        updateDenormalizedFields(newReservation, route);

        // 7. Save to database
        Reservation saved = reservationRepository.save(newReservation);

        // 8. Update route availability (replaces database trigger)
        updateRouteAvailability(route.getId(), request.getDepartureTime(), request.getArrivalTime());

        return saved;
    }

    @Override
    public Reservation cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + reservationId));
        
        // Check if user has permission to cancel (own reservation or admin)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            // Regular user can only cancel their own reservations
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            if (!reservation.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Access denied: You can only cancel your own reservations");
            }
        }
        
        ReservationStatus oldStatus = reservation.getStatus();
        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservation saved = reservationRepository.save(reservation);
        
        // Record status change history (replaces database trigger)
        recordStatusHistory(saved, oldStatus != null ? oldStatus.name() : null, ReservationStatus.CANCELLED.name(), null); // TODO: Add cancellationReason field if needed
        
        // Update route availability after cancellation (replaces database trigger)
        updateRouteAvailability(
                reservation.getRoute().getId(),
                reservation.getTripDetails().getDepartureTime(),
                reservation.getTripDetails().getArrivalTime()
        );
        
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> getAllReservations() {
        // Only admins should call this - regular users should use getMyReservations()
        return reservationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> getMyReservations() {
        // Get reservations for the currently authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        return reservationRepository.findByUserId(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Reservation> getReservationById(Long id) {
        Optional<Reservation> reservation = reservationRepository.findById(id);
        
        if (reservation.isPresent()) {
            // Check if user has access (own reservation or admin)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin) {
                // Regular user can only access their own reservations
                String username = authentication.getName();
                User user = userService.findByUsername(username);
                if (!reservation.get().getUser().getId().equals(user.getId())) {
                    throw new RuntimeException("Access denied: You can only view your own reservations");
                }
            }
        }
        
        return reservation;
    }

    @Override
    @Transactional(readOnly = true)
    public FareCalculationResponse calculateFare(Long routeId, String passengerCategory, Integer seatCount, LocalDateTime departureTime) {
        // Convert string enum to enum type
        PassengerCategory category = PassengerCategory.valueOf(passengerCategory.toUpperCase());
        VehicleClass vehicleClass = VehicleClass.valueOf("STANDARD"); // Default, could be passed as parameter
        
        // Use the new FareCalculationService instead of database function
        // This replaces the database function calculate_reservation_fare()
        return fareCalculationService.calculateFare(routeId, category, vehicleClass, seatCount, departureTime);
    }

    /**
     * Updates denormalized fields on a reservation.
     * This replaces the database trigger update_reservation_denormalized_fields().
     */
    private void updateDenormalizedFields(Reservation reservation, Route route) {
        // Set station names from route
        reservation.setOriginStationName(route.getOriginStation().getName());
        reservation.setDestinationStationName(route.getDestinationStation().getName());

        // Calculate fare using FareCalculationService
        FareCalculationResponse fare = fareCalculationService.calculateFare(
                route.getId(),
                reservation.getPassengerCategory(),
                reservation.getVehicleClass(),
                reservation.getSeatCount(),
                reservation.getTripDetails().getDepartureTime()
        );

        // Set fare fields
        reservation.setBaseFare(fare.getBaseFare());
        reservation.setVatAmount(fare.getVatAmount());
        reservation.setTotalFare(fare.getTotalFare());
    }

    /**
     * Updates route availability for a specific departure time.
     * This replaces the database trigger update_route_availability_trigger().
     * 
     * Note: We rely on SeatAvailabilityService to calculate availability on-demand.
     * The route_availability table is optional for caching - if needed, we can add a RouteAvailabilityService.
     * For now, the calculation is done dynamically when needed, so this method is a placeholder
     * for future implementation if route_availability table caching is required.
     */
    private void updateRouteAvailability(Long routeId, LocalDateTime departureTime, LocalDateTime arrivalTime) {
        // Note: Route availability is now calculated on-demand by SeatAvailabilityService
        // If route_availability table caching is needed for performance, we can implement it here
        // For now, this method serves as a placeholder to maintain the same interface
        // as the database trigger it replaces
    }

    /**
     * Records a status change in the reservation status history.
     * This replaces the database trigger maintain_reservation_status_history().
     */
    private void recordStatusHistory(Reservation reservation, String oldStatus, String newStatus, String changeReason) {
        // Only record if status actually changed
        if (oldStatus != null && oldStatus.equals(newStatus)) {
            return;
        }

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String changedBy = (authentication != null) ? authentication.getName() : "SYSTEM";

            ReservationStatusHistory history = new ReservationStatusHistory(
                    reservation.getId(),
                    oldStatus,
                    newStatus,
                    changedBy,
                    changeReason
            );

            reservationStatusHistoryRepository.save(history);
        } catch (Exception e) {
            // Log error but don't fail the transaction
            System.err.println("Error recording reservation status history: " + e.getMessage());
        }
    }
}