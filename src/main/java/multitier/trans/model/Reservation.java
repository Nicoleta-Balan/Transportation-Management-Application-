package multitier.trans.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "reservations") // table name in the DB, marked class as JPA entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // primary key
    private Long id;

    @NotNull(message = "Route cannot be null")
    @ManyToOne // relation between reservations and routes - many to one (many reservations per route)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @NotBlank(message = "Passenger name cannot be blank")
    private String passengerName;

    @Min(value = 1, message = "Must book at least 1 seat")
    private int seatCount;

    @NotBlank(message = "Reservation status cannot be blank")
    private String status;

    @Embedded // to avoid creating a separate table for this, will be added directly to the 'reservations' table
    private TripTimeDetails tripDetails;

    @NotNull(message = "Passenger category cannot be null")
    @Enumerated(EnumType.STRING) // Tells JPA to store the enum as a String
    @Column(name = "passenger_category")
    private PassengerCategory passengerCategory;

    @NotNull(message = "Vehicle class cannot be null")
    @Enumerated(EnumType.STRING) // Tells JPA to store the enum as a String
    @Column(name = "vehicle_class")
    private VehicleClass vehicleClass;

    public Reservation() {
    }

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

    public TripTimeDetails getTripDetails() {
        return tripDetails;
    }

    public void setTripDetails(TripTimeDetails tripDetails) {
        this.tripDetails = tripDetails;
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
}