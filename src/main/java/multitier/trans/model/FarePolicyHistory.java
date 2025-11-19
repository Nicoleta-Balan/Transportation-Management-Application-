package multitier.trans.model;

import jakarta.persistence.*;
import multitier.trans.model.enums.PassengerCategory;
import multitier.trans.model.enums.VehicleClass;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing the history/audit trail of fare policy changes.
 * Tracks all changes to fare policies (created, updated, deleted, activated, deactivated).
 */
@Entity
@Table(name = "fare_policy_history")
public class FarePolicyHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fare_policy_id", nullable = false)
    private Long farePolicyId;

    @Column(name = "route_id", nullable = false)
    private Long routeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "passenger_category", nullable = false, length = 20)
    private PassengerCategory passengerCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_class", nullable = false, length = 20)
    private VehicleClass vehicleClass;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "old_price", precision = 10, scale = 2)
    private BigDecimal oldPrice;

    @Column(name = "change_type", nullable = false, length = 20)
    private String changeType; // 'CREATED', 'UPDATED', 'DELETED', 'ACTIVATED', 'DEACTIVATED'

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "changed_by", length = 100)
    private String changedBy;

    @Column(name = "change_reason", columnDefinition = "TEXT")
    private String changeReason;

    public FarePolicyHistory() {
    }

    public FarePolicyHistory(Long farePolicyId, Long routeId, PassengerCategory passengerCategory,
                            VehicleClass vehicleClass, BigDecimal basePrice, BigDecimal oldPrice,
                            String changeType, LocalDate effectiveFrom, LocalDate effectiveTo,
                            String changedBy) {
        this.farePolicyId = farePolicyId;
        this.routeId = routeId;
        this.passengerCategory = passengerCategory;
        this.vehicleClass = vehicleClass;
        this.basePrice = basePrice;
        this.oldPrice = oldPrice;
        this.changeType = changeType;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.changedAt = LocalDateTime.now();
        this.changedBy = changedBy;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFarePolicyId() {
        return farePolicyId;
    }

    public void setFarePolicyId(Long farePolicyId) {
        this.farePolicyId = farePolicyId;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public PassengerCategory getPassengerCategory() {
        return passengerCategory;
    }

    public void setPassengerCategory(PassengerCategory passengerCategory) {
        this.passengerCategory = passengerCategory;
    }

    public VehicleClass getVehicleClass() {
        return vehicleClass;
    }

    public void setVehicleClass(VehicleClass vehicleClass) {
        this.vehicleClass = vehicleClass;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(BigDecimal oldPrice) {
        this.oldPrice = oldPrice;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }
}

