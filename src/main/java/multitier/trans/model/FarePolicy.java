package multitier.trans.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import multitier.trans.model.enums.PassengerCategory;
import multitier.trans.model.enums.PolicyStatus;
import multitier.trans.model.enums.VehicleClass;
import multitier.trans.model.converter.PolicyStatusConverter;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Domain Model: Fare Policy & Cost.
 * This entity defines a specific pricing rule in the system.
 * It links a Route, a Passenger Category, and a Vehicle Class to a specific price.
 */
@Entity
@Table(name = "fare_policies")
@EntityListeners(FarePolicyHistoryListener.class)
public class FarePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // A policy is linked to a specific Route
    @NotNull(message = "Route cannot be null")
    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    // The policy applies to a specific passenger type (from SCRUM-32)
    @NotNull(message = "Passenger category cannot be null")
    @Enumerated(EnumType.STRING) // This is important! It stores "ADULT" in the DB, not 0.
    @Column(name = "passenger_category", nullable = false)
    private PassengerCategory passengerCategory;

    // The policy applies to a specific vehicle class (from SCRUM-32)
    @NotNull(message = "Vehicle class cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_class", nullable = false)
    private VehicleClass vehicleClass;

    // The cost (price) defined by this policy
    @NotNull
    @DecimalMin(value = "0.00", inclusive = true, message = "Price cannot be negative")
    @Column(name = "base_price", nullable = false)
    private BigDecimal price;

    @NotNull(message = "Effective from date cannot be null")
    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @NotNull(message = "Status cannot be null")
    @Convert(converter = PolicyStatusConverter.class)
    @Column(nullable = false, length = 20)
    private PolicyStatus status = PolicyStatus.ACTIVE;

    // --- Constructors ---

    public FarePolicy() {
    }

    /**
     * Checks if this fare policy is active at the given date.
     */
    public boolean isActiveAt(java.time.LocalDateTime date) {
        if (date == null) {
            date = java.time.LocalDateTime.now();
        }
        LocalDate checkDate = date.toLocalDate();
        return PolicyStatus.ACTIVE.equals(status) &&
               !checkDate.isBefore(effectiveFrom) &&
               (effectiveTo == null || !checkDate.isAfter(effectiveTo));
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
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

    public PolicyStatus getStatus() {
        return status;
    }

    public void setStatus(PolicyStatus status) {
        this.status = status;
    }
}