package multitier.trans.repository;

import multitier.trans.model.VatRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Spring Data JPA Repository for the VatRate entity.
 */
@Repository
public interface VatRateRepository extends JpaRepository<VatRate, Long> {

    /**
     * Finds the active VAT rate for the current timestamp.
     * Returns the most recent VAT rate that is effective at the current time.
     */
    @Query("SELECT v FROM VatRate v WHERE v.effectiveFrom <= :date " +
           "AND (v.effectiveTo IS NULL OR v.effectiveTo > :date) " +
           "ORDER BY v.effectiveFrom DESC")
    Optional<VatRate> findActiveVatRateForDate(@Param("date") LocalDateTime date);

    /**
     * Finds the active VAT rate for the current timestamp.
     */
    default Optional<VatRate> findCurrentVatRate() {
        return findActiveVatRateForDate(LocalDateTime.now());
    }

    /**
     * Checks for overlapping VAT rates.
     * Used for validation before saving a new or updated VAT rate.
     */
    @Query("SELECT COUNT(v) > 0 FROM VatRate v WHERE v.id != :excludeId " +
           "AND v.effectiveFrom < :maxEffectiveTo " +
           "AND (v.effectiveTo IS NULL OR v.effectiveTo > :effectiveFrom)")
    boolean existsOverlappingVatRate(
            @Param("effectiveFrom") LocalDateTime effectiveFrom,
            @Param("maxEffectiveTo") LocalDateTime maxEffectiveTo,
            @Param("excludeId") Long excludeId
    );
}

