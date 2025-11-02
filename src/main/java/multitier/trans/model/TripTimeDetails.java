package multitier.trans.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Implements (Create Value Object - Trip/Time Details).
 *
 * This is a "Value Object" that will be embedded into other entities (like Reservation).
 * It groups related time details together.
 * @Embeddable means this class's fields will be stored as columns
 * in the table of the entity that owns it.
 */

@Embeddable
public class TripTimeDetails {

    @NotNull(message = "Departure time cannot be null")
    private LocalDateTime departureTime;

    @NotNull(message = "Arrival time cannot be null")
    private LocalDateTime arrivalTime;

    // --- Constructors ---

    public TripTimeDetails() {
    }

    public TripTimeDetails(LocalDateTime departureTime, LocalDateTime arrivalTime) {
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    // --- Getters and Setters ---

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
}