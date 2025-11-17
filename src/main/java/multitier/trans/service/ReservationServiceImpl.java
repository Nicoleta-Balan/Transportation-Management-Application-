package multitier.trans.service;

import multitier.trans.dto.CreateReservationRequest;
import multitier.trans.dto.FareCalculationResponse;
import multitier.trans.model.Reservation;
import multitier.trans.model.Route;
import multitier.trans.model.TripTimeDetails;
import multitier.trans.repository.ReservationRepository;
import multitier.trans.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public ReservationServiceImpl(ReservationRepository reservationRepository, RouteRepository routeRepository) {
        this.reservationRepository = reservationRepository;
        this.routeRepository = routeRepository;
    }

    /**
     * Reservation Creation (MODIFIED)
     */
    @Override
    public Reservation createReservation(CreateReservationRequest request) {
        // 1. Find the Route
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
        newReservation.setPassengerEmail(request.getPassengerEmail());
        newReservation.setPassengerPhone(request.getPassengerPhone());
        newReservation.setSeatCount(request.getSeatCount());
        newReservation.setTripDetails(tripDetails);
        newReservation.setStatus("CONFIRMED");


        // 4. Set the new fare details from the DTO
        newReservation.setPassengerCategory(request.getPassengerCategory());
        newReservation.setVehicleClass(request.getVehicleClass());

        // 5. Save to database
        Reservation saved = reservationRepository.save(newReservation);

        // Refresh to include denormalized fields populated by database triggers
        return reservationRepository.findById(saved.getId())
                .orElseThrow(() -> new RuntimeException("Reservation not found after creation: " + saved.getId()));
    }

    @Override
    public Reservation cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + reservationId));
        reservation.setStatus("CANCELLED");
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