package multitier.trans.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import multitier.trans.model.enums.PassengerCategory;
import multitier.trans.model.enums.VehicleClass;
import java.math.BigDecimal;

/**
 * Implements SCRUM-33 (Domain Model Implementation: Reservation & Booking).
 * MODIFIED to include fare details needed for SCRUM-34.
 */
@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User cannot be null")
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Route cannot be null")
    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    // Passenger details are optional - will default to user's info if not provided
    @Column(name = "passenger_name")
    private String passengerName;

    @Column(name = "passenger_email")
    private String passengerEmail;

    @Column(name = "passenger_phone")
    private String passengerPhone;

    @Min(value = 1, message = "Must book at least 1 seat")
    @Column(name = "seat_count", nullable = false)
    private int seatCount;

    @NotBlank(message = "Reservation status cannot be blank")
    @Column(nullable = false)
    private String status; // e.g., "CONFIRMED", "CANCELLED", "PENDING"

    // Trip time details (embedded)
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "departureTime", column = @Column(name = "departure_time", nullable = false)),
        @AttributeOverride(name = "arrivalTime", column = @Column(name = "arrival_time", nullable = false))
    })
    private TripTimeDetails tripDetails;

    // Denormalized fields (populated by database triggers)
    @Column(name = "origin_station_name", insertable = false, updatable = false)
    private String originStationName;

    @Column(name = "destination_station_name", insertable = false, updatable = false)
    private String destinationStationName;

    @Column(name = "base_fare", insertable = false, updatable = false)
    private BigDecimal baseFare;

    @Column(name = "vat_amount", insertable = false, updatable = false)
    private BigDecimal vatAmount;

    @Column(name = "total_fare", insertable = false, updatable = false)
    private BigDecimal totalFare;



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

    public String getPassengerEmail() {
        return passengerEmail;
    }

    public void setPassengerEmail(String passengerEmail) {
        this.passengerEmail = passengerEmail;
    }

    public String getPassengerPhone() {
        return passengerPhone;
    }

    public void setPassengerPhone(String passengerPhone) {
        this.passengerPhone = passengerPhone;
    }

    public String getOriginStationName() {
        return originStationName;
    }

    public void setOriginStationName(String originStationName) {
        this.originStationName = originStationName;
    }

    public String getDestinationStationName() {
        return destinationStationName;
    }

    public void setDestinationStationName(String destinationStationName) {
        this.destinationStationName = destinationStationName;
    }

    public BigDecimal getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(BigDecimal baseFare) {
        this.baseFare = baseFare;
    }

    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(BigDecimal vatAmount) {
        this.vatAmount = vatAmount;
    }

    public BigDecimal getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(BigDecimal totalFare) {
        this.totalFare = totalFare;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}