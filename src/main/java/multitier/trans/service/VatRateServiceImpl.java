package multitier.trans.service;

import multitier.trans.model.VatRate;
import multitier.trans.repository.VatRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Service implementation for VAT rate operations.
 * Replaces database function get_current_vat_rate(), get_vat_rate_for_date(), and validation triggers.
 */
@Service
@Transactional
public class VatRateServiceImpl implements VatRateService {

    private final VatRateRepository vatRateRepository;

    @Autowired
    public VatRateServiceImpl(VatRateRepository vatRateRepository) {
        this.vatRateRepository = vatRateRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCurrentVatRate() {
        return getVatRateForDate(LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getVatRateForDate(LocalDateTime date) {
        final LocalDateTime checkDate = (date == null) ? LocalDateTime.now() : date;

        VatRate vatRate = vatRateRepository.findActiveVatRateForDate(checkDate)
                .orElseThrow(() -> new RuntimeException(
                        "No active VAT rate found for date: " + checkDate));

        return vatRate.getRatePercentage();
    }

    @Override
    @Transactional(readOnly = true)
    public VatRate getVatRateEntityForDate(LocalDateTime date) {
        final LocalDateTime checkDate = (date == null) ? LocalDateTime.now() : date;

        return vatRateRepository.findActiveVatRateForDate(checkDate)
                .orElseThrow(() -> new RuntimeException(
                        "No active VAT rate found for date: " + checkDate));
    }

    @Override
    public void validateVatRate(VatRate vatRate) {
        // Check if effective_to is after effective_from
        if (vatRate.getEffectiveTo() != null && 
            !vatRate.getEffectiveTo().isAfter(vatRate.getEffectiveFrom())) {
            throw new RuntimeException("Effective end date must be after effective start date");
        }

        // Check for overlapping VAT rates (only one active VAT rate at a time)
        Long excludeId = (vatRate.getId() != null) ? vatRate.getId() : -1L;
        LocalDateTime maxEffectiveTo = (vatRate.getEffectiveTo() != null) 
                ? vatRate.getEffectiveTo() 
                : LocalDateTime.of(9999, 12, 31, 23, 59, 59);

        boolean hasOverlap = vatRateRepository.existsOverlappingVatRate(
                vatRate.getEffectiveFrom(),
                maxEffectiveTo,
                excludeId
        );

        if (hasOverlap) {
            throw new RuntimeException("Overlapping VAT rate exists for the specified date range");
        }
    }

    @Override
    public VatRate saveVatRate(VatRate vatRate) {
        validateVatRate(vatRate);
        return vatRateRepository.save(vatRate);
    }
}

