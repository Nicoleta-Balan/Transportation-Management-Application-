package multitier.trans.service;

import multitier.trans.dto.CreateReservationRequest;
import multitier.trans.dto.FareCalculationResponse;
import multitier.trans.model.Reservation;
import multitier.trans.model.Route;
import multitier.trans.model.TripTimeDetails;
import multitier.trans.model.User;
import multitier.trans.repository.ReservationRepository;
import multitier.trans.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service Implementation for Reservation logic.
 */
@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final RouteRepository routeRepository;
    private final multitier.trans.service.UserService userService;

    @Autowired
    public ReservationServiceImpl(
            ReservationRepository reservationRepository, 
            RouteRepository routeRepository,
            multitier.trans.service.UserService userService) {
        this.reservationRepository = reservationRepository;
        this.routeRepository = routeRepository;
        this.userService = userService;
    }

    /**
     * Reservation Creation (MODIFIED)
     * Automatically associates the reservation with the currently authenticated user
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

        // 3. Create the Value Object
        TripTimeDetails tripDetails = new TripTimeDetails(
                request.getDepartureTime(),
                request.getArrivalTime()
        );

        // 4. Create the new Reservation entity
        Reservation newReservation = new Reservation();
        newReservation.setUser(user); // Associate with the authenticated user
        newReservation.setRoute(route);
        
        // Set passenger details - if not provided, default to user's details
        String passengerName = request.getPassengerName();
        if (passengerName == null || passengerName.trim().isEmpty()) {
            // Default to user's full name
            passengerName = (user.getFirstName() != null ? user.getFirstName() : "") + 
                          (user.getLastName() != null ? " " + user.getLastName() : "").trim();
            if (passengerName.isEmpty()) {
                passengerName = user.getUsername(); // Fallback to username
            }
        }
        newReservation.setPassengerName(passengerName);
        
        // Default email to user's email if not provided
        newReservation.setPassengerEmail(
            request.getPassengerEmail() != null && !request.getPassengerEmail().trim().isEmpty()
                ? request.getPassengerEmail()
                : user.getEmail()
        );
        
        // Default phone to user's phone if not provided
        newReservation.setPassengerPhone(
            request.getPassengerPhone() != null && !request.getPassengerPhone().trim().isEmpty()
                ? request.getPassengerPhone()
                : user.getPhone()
        );
        newReservation.setSeatCount(request.getSeatCount());
        newReservation.setTripDetails(tripDetails);
        newReservation.setStatus("CONFIRMED");


        // 5. Set the new fare details from the DTO
        newReservation.setPassengerCategory(request.getPassengerCategory());
        newReservation.setVehicleClass(request.getVehicleClass());

        // 6. Save to database
        Reservation saved = reservationRepository.save(newReservation);

        // Refresh to include denormalized fields populated by database triggers
        return reservationRepository.findById(saved.getId())
                .orElseThrow(() -> new RuntimeException("Reservation not found after creation: " + saved.getId()));
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
        
        reservation.setStatus("CANCELLED");
        return reservationRepository.save(reservation);
    }

    @Override
    public List<Reservation> getAllReservations() {
        // Only admins should call this - regular users should use getMyReservations()
        return reservationRepository.findAll();
    }

    @Override
    public List<Reservation> getMyReservations() {
        // Get reservations for the currently authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        return reservationRepository.findByUserId(user.getId());
    }

    @Override
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
    public FareCalculationResponse calculateFare(Long routeId, String passengerCategory, Integer seatCount, LocalDateTime departureTime) {
        // Call the database function
        Map<String, Object> result = reservationRepository.calculateFare(
                routeId.intValue(),
                passengerCategory,
                seatCount,
                departureTime
        );
        
        // Extract values from the result map
        // PostgreSQL returns DECIMAL as BigDecimal, but we need to handle it safely
        Object baseFareObj = result.get("base_fare");
        Object vatAmountObj = result.get("vat_amount");
        Object totalFareObj = result.get("total_fare");
        Object vatRateObj = result.get("vat_rate");
        
        BigDecimal baseFare = baseFareObj instanceof BigDecimal 
                ? (BigDecimal) baseFareObj 
                : BigDecimal.valueOf(((Number) baseFareObj).doubleValue());
        
        BigDecimal vatAmount = vatAmountObj instanceof BigDecimal
                ? (BigDecimal) vatAmountObj
                : BigDecimal.valueOf(((Number) vatAmountObj).doubleValue());
        
        BigDecimal totalFare = totalFareObj instanceof BigDecimal
                ? (BigDecimal) totalFareObj
                : BigDecimal.valueOf(((Number) totalFareObj).doubleValue());
        
        BigDecimal vatRate = vatRateObj instanceof BigDecimal
                ? (BigDecimal) vatRateObj
                : BigDecimal.valueOf(((Number) vatRateObj).doubleValue());
        
        return new FareCalculationResponse(baseFare, vatAmount, totalFare, vatRate);
    }
}