package multitier.trans.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import multitier.trans.model.enums.PassengerCategory;
import multitier.trans.model.enums.VehicleClass;

/**
 * Implements Domain Model Implementation: Reservation & Booking
 * Includes fare details
 */

@Entity
@Table(name = "reservations")
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

    @NotBlank(message = "Reservation status cannot be blank")
    private String status; // e.g., "CONFIRMED", "CANCELLED", "PENDING"

    @Embedded
    private TripTimeDetails tripDetails;

    /**
     * Stores the passenger category for this specific reservation (e.g., ADULT).
     * This is required to find the price that was paid.
     */

    @NotNull(message = "Passenger category cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "passenger_category", nullable = false)
    private PassengerCategory passengerCategory;

    /**
     * Stores the vehicle class for this specific reservation (e.g., STANDARD).
     * This is required to find the price that was paid.
     */

    @NotNull(message = "Vehicle class cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_class", nullable = false)
    private VehicleClass vehicleClass;


    public Reservation() {
    }

    public Long getId() {
        return id;
    }

    // ... other getters/setters ...
    public TripTimeDetails getTripDetails() {
        return tripDetails;
    }

    public void setTripDetails(TripTimeDetails tripDetails) {
        this.tripDetails = tripDetails;
    }

    // --- GETTERS/SETTERS ---
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

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public int getSeatCount() {
        return seatCount;
    }

    public void setSeatCount(int seatCount) {
        this.seatCount = seatCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}