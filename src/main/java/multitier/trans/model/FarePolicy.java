package multitier.trans.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import multitier.trans.model.enums.PassengerCategory;
import multitier.trans.model.enums.VehicleClass;

@Entity
@Table(name = "fare_policies")
@Data  // Lombok: Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor  // Required by JPA
public class FarePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // A policy is linked to a specific Route
    @NotNull(message = "Route cannot be null")
    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    // The policy applies to a specific passenger type
    @NotNull(message = "Passenger category cannot be null")
    @Enumerated(EnumType.STRING) // This is important! It stores "ADULT" in the DB, not 0.
    @Column(name = "passenger_category", nullable = false)
    private PassengerCategory passengerCategory;

    // The policy applies to a specific vehicle class
    @NotNull(message = "Vehicle class cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_class", nullable = false)
    private VehicleClass vehicleClass;

    // The cost (price) defined by this policy
    @NotNull
    @Min(value = 0, message = "Price cannot be negative")
    @Column(nullable = false)
    private Double price; //Intended as base price per trip
}