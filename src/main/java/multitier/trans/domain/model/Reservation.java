package multitier.trans.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 *
 * This is the main @Entity for a reservation. It will create a 'reservations' table.
 */

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * This links the Reservation to a specific Route.
     * Many Reservations can be made for one Route.
     */

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

    /**
     * This uses an @Embedded Value Object
     * The fields (departureTime, arrivalTime) from TripTimeDetails
     * will be added directly to the 'reservations' table.
     */

    @Embedded
    private TripTimeDetails tripDetails;

    // --- Constructors ---

    public Reservation() {
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
}

