package multitier.trans.repository;

import multitier.trans.model.FarePolicy;
import multitier.trans.model.enums.PassengerCategory;
import multitier.trans.model.enums.PolicyStatus;
import multitier.trans.model.enums.VehicleClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    /**
     * Finds the active fare policy for a route, category, class, and date.
     * Replaces database function get_active_fare_policy().
     */
    @Query("SELECT f FROM FarePolicy f WHERE f.route.id = :routeId " +
           "AND f.passengerCategory = :category " +
           "AND f.vehicleClass = :vehicleClass " +
           "AND f.status = :status " +
           "AND f.effectiveFrom <= :date " +
           "AND (f.effectiveTo IS NULL OR f.effectiveTo > :date) " +
           "ORDER BY f.effectiveFrom DESC")
    default Optional<FarePolicy> findActiveFarePolicy(
            Long routeId,
            PassengerCategory category,
            VehicleClass vehicleClass,
            LocalDate date
    ) {
        return findActiveFarePolicy(routeId, category, vehicleClass, PolicyStatus.ACTIVE, date);
    }
    
    @Query("SELECT f FROM FarePolicy f WHERE f.route.id = :routeId " +
           "AND f.passengerCategory = :category " +
           "AND f.vehicleClass = :vehicleClass " +
           "AND f.status = :status " +
           "AND f.effectiveFrom <= :date " +
           "AND (f.effectiveTo IS NULL OR f.effectiveTo > :date) " +
           "ORDER BY f.effectiveFrom DESC")
    Optional<FarePolicy> findActiveFarePolicy(
            @Param("routeId") Long routeId,
            @Param("category") PassengerCategory category,
            @Param("vehicleClass") VehicleClass vehicleClass,
            @Param("status") PolicyStatus status,
            @Param("date") LocalDate date
    );

    /**
     * Checks for overlapping active fare policies.
     * Used for validation before saving a new or updated fare policy.
     */
    @Query("SELECT COUNT(f) > 0 FROM FarePolicy f WHERE f.route.id = :routeId " +
           "AND f.passengerCategory = :category " +
           "AND f.vehicleClass = :vehicleClass " +
           "AND f.status = :status " +
           "AND f.id != :excludeId " +
           "AND f.effectiveFrom < :maxEffectiveTo " +
           "AND (f.effectiveTo IS NULL OR f.effectiveTo > :effectiveFrom)")
    boolean existsOverlappingActivePolicy(
            @Param("routeId") Long routeId,
            @Param("category") PassengerCategory category,
            @Param("vehicleClass") VehicleClass vehicleClass,
            @Param("status") PolicyStatus status,
            @Param("effectiveFrom") LocalDate effectiveFrom,
            @Param("maxEffectiveTo") LocalDate maxEffectiveTo,
            @Param("excludeId") Long excludeId
    );
}