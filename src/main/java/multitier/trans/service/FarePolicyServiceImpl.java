package multitier.trans.service;

import multitier.trans.model.FarePolicy;
import multitier.trans.model.enums.PassengerCategory;
import multitier.trans.model.enums.VehicleClass;
import multitier.trans.repository.FarePolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Service implementation for fare policy operations.
 * Replaces database function get_active_fare_policy() and validation triggers.
 */
@Service
@Transactional
public class FarePolicyServiceImpl implements FarePolicyService {

    private final FarePolicyRepository farePolicyRepository;

    @Autowired
    public FarePolicyServiceImpl(FarePolicyRepository farePolicyRepository) {
        this.farePolicyRepository = farePolicyRepository;
    }

    @Override
    public BigDecimal getActiveFarePolicyPrice(Long routeId, PassengerCategory category, 
                                               VehicleClass vehicleClass, LocalDateTime date) {
        FarePolicy policy = getActiveFarePolicy(routeId, category, vehicleClass, date);
        return policy.getPrice();
    }

    @Override
    @Transactional(readOnly = true)
    public FarePolicy getActiveFarePolicy(Long routeId, PassengerCategory category, 
                                          VehicleClass vehicleClass, LocalDateTime date) {
        if (date == null) {
            date = LocalDateTime.now();
        }

        return farePolicyRepository.findActiveFarePolicy(
                routeId, category, vehicleClass, date.toLocalDate())
                .orElseThrow(() -> new RuntimeException(
                        String.format("No active fare policy found for route %d, category %s, class %s",
                                routeId, category, vehicleClass)));
    }

    @Override
    public void validateFarePolicy(FarePolicy farePolicy) {
        // Check if effective_to is after effective_from
        if (farePolicy.getEffectiveTo() != null && 
            !farePolicy.getEffectiveTo().isAfter(farePolicy.getEffectiveFrom())) {
            throw new RuntimeException("Effective end date must be after effective start date");
        }

        // Check for overlapping active policies (only one active policy per route/category/class at a time)
        if (multitier.trans.model.enums.PolicyStatus.ACTIVE.equals(farePolicy.getStatus())) {
            Long excludeId = (farePolicy.getId() != null) ? farePolicy.getId() : -1L;
            LocalDate maxEffectiveTo = (farePolicy.getEffectiveTo() != null) 
                    ? farePolicy.getEffectiveTo() 
                    : LocalDate.of(9999, 12, 31);

            boolean hasOverlap = farePolicyRepository.existsOverlappingActivePolicy(
                    farePolicy.getRoute().getId(),
                    farePolicy.getPassengerCategory(),
                    farePolicy.getVehicleClass(),
                    multitier.trans.model.enums.PolicyStatus.ACTIVE,
                    farePolicy.getEffectiveFrom(),
                    maxEffectiveTo,
                    excludeId
            );

            if (hasOverlap) {
                throw new RuntimeException(
                        String.format("Overlapping active fare policy exists for route %d, category %s, class %s",
                                farePolicy.getRoute().getId(),
                                farePolicy.getPassengerCategory(),
                                farePolicy.getVehicleClass()));
            }
        }
    }

    @Override
    public FarePolicy saveFarePolicy(FarePolicy farePolicy) {
        validateFarePolicy(farePolicy);
        return farePolicyRepository.save(farePolicy);
    }
}

