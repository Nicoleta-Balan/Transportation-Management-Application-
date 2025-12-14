package multitier.trans.repository;

import multitier.trans.model.FarePolicy;
import multitier.trans.model.enums.PassengerCategory;
import multitier.trans.model.enums.VehicleClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RepositoryRestResource(exported = false)  // Disable automatic REST exposure
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