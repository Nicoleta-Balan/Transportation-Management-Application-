package multitier.trans.service;

import multitier.trans.model.VatRate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Service interface for VAT rate operations.
 */
public interface VatRateService {

    /**
     * Gets the current VAT rate (for the current timestamp).
     * 
     * @return The current VAT rate percentage (e.g., 19.00 for 19%)
     * @throws RuntimeException if no active VAT rate is found
     */
    BigDecimal getCurrentVatRate();

    /**
     * Gets the VAT rate for a specific date.
     * 
     * @param date The date to check
     * @return The VAT rate percentage for that date
     * @throws RuntimeException if no active VAT rate is found for the date
     */
    BigDecimal getVatRateForDate(LocalDateTime date);

    /**
     * Gets the VatRate entity for a specific date.
     * 
     * @param date The date to check
     * @return The VatRate entity
     * @throws RuntimeException if no active VAT rate is found for the date
     */
    VatRate getVatRateEntityForDate(LocalDateTime date);

    /**
     * Validates a VAT rate before saving.
     * Checks date validity and overlapping rates.
     * 
     * @param vatRate The VAT rate to validate
     * @throws RuntimeException if validation fails
     */
    void validateVatRate(VatRate vatRate);

    /**
     * Saves a VAT rate with validation.
     * 
     * @param vatRate The VAT rate to save
     * @return The saved VAT rate
     * @throws RuntimeException if validation fails
     */
    VatRate saveVatRate(VatRate vatRate);
}

