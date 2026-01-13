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
import multitier.trans.exception.BusinessException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations", indexes = {
    @Index(name = "idx_reservation_route", columnList = "route_id"),
    @Index(name = "idx_reservation_status", columnList = "status"),
    @Index(name = "idx_reservation_passenger", columnList = "passengerName"),
    @Index(name = "idx_reservation_user", columnList = "user_id")
})
@Getter  // Lombok: Generates getters
@Setter  // Lombok: Generates setters
@NoArgsConstructor  // Required by JPA
// Using @Getter/@Setter instead of @Data to avoid equals/hashCode issues with @ManyToOne and @Embedded relationships
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link reservation to authenticated user (optional for guest bookings)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull(message = "Route cannot be null")
    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @NotBlank(message = "Passenger name cannot be blank")
    private String passengerName;

    @Min(value = 1, message = "Must book at least 1 seat")
    private int seatCount;

    // Comma-separated list of selected seat numbers (e.g., "1A,1B,2C")
    @Column(name = "selected_seats")
    private String selectedSeats;

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

    // Total price paid for this reservation
    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;

    // Currency used for payment
    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        
        // Validate that departure time is not in the past
        // We allow a small buffer (e.g. 5 minutes) for network latency/clock skew
        if (this.tripDetails != null && this.tripDetails.getDepartureTime() != null) {
            if (this.tripDetails.getDepartureTime().isBefore(LocalDateTime.now().minusMinutes(5))) {
                throw new BusinessException("Cannot create reservation for a past departure time.");
            }
        }
    }
}