package multitier.trans.service;

import multitier.trans.model.FarePolicy;
import multitier.trans.model.enums.PassengerCategory;
import multitier.trans.model.enums.VehicleClass;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Service interface for fare policy operations.
 */
public interface FarePolicyService {

    /**
     * Gets the active fare policy price for a route, category, class, and date.
     * Replaces database function get_active_fare_policy().
     * 
     * @param routeId The route ID
     * @param category The passenger category
     * @param vehicleClass The vehicle class
     * @param date The date to check (defaults to current date if null)
     * @return The base price for the fare policy
     * @throws RuntimeException if no active fare policy is found
     */
    BigDecimal getActiveFarePolicyPrice(Long routeId, PassengerCategory category, 
                                       VehicleClass vehicleClass, LocalDateTime date);

    /**
     * Gets the active fare policy entity for a route, category, class, and date.
     * 
     * @param routeId The route ID
     * @param category The passenger category
     * @param vehicleClass The vehicle class
     * @param date The date to check (defaults to current date if null)
     * @return The FarePolicy entity
     * @throws RuntimeException if no active fare policy is found
     */
    FarePolicy getActiveFarePolicy(Long routeId, PassengerCategory category, 
                                   VehicleClass vehicleClass, LocalDateTime date);

    /**
     * Validates a fare policy before saving.
     * Checks date validity and overlapping active policies.
     * 
     * @param farePolicy The fare policy to validate
     * @throws RuntimeException if validation fails
     */
    void validateFarePolicy(FarePolicy farePolicy);

    /**
     * Saves a fare policy with validation.
     * 
     * @param farePolicy The fare policy to save
     * @return The saved fare policy
     * @throws RuntimeException if validation fails
     */
    FarePolicy saveFarePolicy(FarePolicy farePolicy);
}

