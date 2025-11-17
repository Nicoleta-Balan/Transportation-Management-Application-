package multitier.trans.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import multitier.trans.model.enums.PassengerCategory;
import multitier.trans.model.enums.VehicleClass;
import java.time.LocalDateTime;

/**
 * DTO for handling a new reservation request.
 * MODIFIED to include fare details needed for SCRUM-34.
 */
public class CreateReservationRequest {

    @NotNull(message = "Route ID cannot be null")
    private Long routeId;

    // Optional: If not provided, will use the authenticated user's details
    private String passengerName;

    // Optional: If not provided, will use the authenticated user's email
    // Note: @Email allows null, but we validate format if provided
    private String passengerEmail;

    @Size(max = 20, message = "Passenger phone number must be at most 20 characters")
    private String passengerPhone;

    @Min(value = 1, message = "Must book at least 1 seat")
    private int seatCount;

    @NotNull(message = "Departure time cannot be null")
    private LocalDateTime departureTime;

    @NotNull(message = "Arrival time cannot be null")
    private LocalDateTime arrivalTime;

    @NotNull(message = "Passenger category must be specified")
    private PassengerCategory passengerCategory;

    @NotNull(message = "Vehicle class must be specified")
    private VehicleClass vehicleClass;

    // --- START: Getters and Setters (FIX) ---
    // These methods were missing, causing the build failure.

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
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

    public int getSeatCount() {
        return seatCount;
    }

    public void setSeatCount(int seatCount) {
        this.seatCount = seatCount;
    }

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
    // --- END: Getters and Setters (FIX) ---
}