package multitier.trans.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a VAT (Value Added Tax) rate with effective dates.
 * VAT rates can change over time, so this entity tracks the rate and when it's effective.
 */
@Entity
@Table(name = "vat_rates")
public class VatRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Rate percentage cannot be null")
    @DecimalMin(value = "0.00", inclusive = true, message = "Rate percentage cannot be negative")
    @DecimalMax(value = "100.00", inclusive = true, message = "Rate percentage cannot exceed 100")
    @Column(name = "rate_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal ratePercentage;

    @NotNull(message = "Effective from date cannot be null")
    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    public VatRate() {
    }

    public VatRate(BigDecimal ratePercentage, LocalDateTime effectiveFrom) {
        this.ratePercentage = ratePercentage;
        this.effectiveFrom = effectiveFrom;
    }

    /**
     * Checks if this VAT rate is active at the given date.
     */
    public boolean isActiveAt(LocalDateTime date) {
        if (date == null) {
            date = LocalDateTime.now();
        }
        return !date.isBefore(effectiveFrom) && 
               (effectiveTo == null || !date.isAfter(effectiveTo));
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getRatePercentage() {
        return ratePercentage;
    }

    public void setRatePercentage(BigDecimal ratePercentage) {
        this.ratePercentage = ratePercentage;
    }

    public LocalDateTime getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDateTime effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDateTime getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDateTime effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}

