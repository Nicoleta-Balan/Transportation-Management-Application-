package multitier.trans.service;

import multitier.trans.dto.FareCalculationResponse;
import multitier.trans.model.enums.PassengerCategory;
import multitier.trans.model.enums.VehicleClass;

import java.time.LocalDateTime;

/**
 * Service interface for fare calculation operations.
 * Replaces database function calculate_reservation_fare().
 */
public interface FareCalculationService {

    /**
     * Calculates the fare for a reservation including VAT.
     * Replaces database function calculate_reservation_fare().
     * 
     * @param routeId The route ID
     * @param passengerCategory The passenger category
     * @param vehicleClass The vehicle class
     * @param seatCount The number of seats
     * @param departureTime The departure time (used for fare policy and VAT rate lookup)
     * @return FareCalculationResponse with base fare, VAT amount, total fare, and VAT rate
     */
    FareCalculationResponse calculateFare(Long routeId, PassengerCategory passengerCategory,
                                         VehicleClass vehicleClass, Integer seatCount,
                                         LocalDateTime departureTime);
}

