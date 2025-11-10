package multitier.trans.dto;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;

public class RouteTimetableEntryRequest {

    @NotNull(message = "Service day is required")
    private DayOfWeek serviceDay;

    @NotNull(message = "Departure time is required")
    private LocalTime departureTime;

    @NotNull(message = "Arrival time is required")
    private LocalTime arrivalTime;

    private String notes;

    public DayOfWeek getServiceDay() {
        return serviceDay;
    }

    public void setServiceDay(DayOfWeek serviceDay) {
        this.serviceDay = serviceDay;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

