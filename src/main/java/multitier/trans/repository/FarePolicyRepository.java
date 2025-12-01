package multitier.trans.repository;

import multitier.trans.model.FarePolicy;
import multitier.trans.model.PassengerCategory;
import multitier.trans.model.VehicleClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for the FarePolicy entity.
 * Provides CRUD methods automatically.
 */
@Repository
public interface FarePolicyRepository extends JpaRepository<FarePolicy, Long> {

    /**
     * This is a custom query that Spring Data JPA will create automatically.
     * It allows our "brain" (the service) to ask:
     * "What is the price for this specific route, for this passenger, in this class?"
     */
    Optional<FarePolicy> findByRouteIdAndPassengerCategoryAndVehicleClass(
            Long routeId,
            PassengerCategory passengerCategory,
            VehicleClass vehicleClass
    );
}