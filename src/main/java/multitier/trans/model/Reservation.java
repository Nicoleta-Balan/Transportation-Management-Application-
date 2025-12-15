package multitier.trans.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import multitier.trans.model.enums.PassengerCategory;
import multitier.trans.model.enums.ReservationStatus;
import multitier.trans.model.enums.VehicleClass;

@Entity
@Table(name = "reservations", indexes = {
    @Index(name = "idx_reservation_route", columnList = "route_id"),
    @Index(name = "idx_reservation_status", columnList = "status"),
    @Index(name = "idx_reservation_passenger", columnList = "passengerName")
})
@Getter  // Lombok: Generates getters
@Setter  // Lombok: Generates setters
@NoArgsConstructor  // Required by JPA
// Using @Getter/@Setter instead of @Data to avoid equals/hashCode issues with @ManyToOne and @Embedded relationships
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Route cannot be null")
    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @NotBlank(message = "Passenger name cannot be blank")
    private String passengerName;

    @Min(value = 1, message = "Must book at least 1 seat")
    private int seatCount;

    @NotNull(message = "Reservation status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status;

    @Embedded
    private TripTimeDetails tripDetails;

    @NotNull(message = "Passenger category cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "passenger_category", nullable = false)
    private PassengerCategory passengerCategory;

    @NotNull(message = "Vehicle class cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_class", nullable = false)
    private VehicleClass vehicleClass;
}