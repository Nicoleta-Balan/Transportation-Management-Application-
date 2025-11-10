package multitier.trans.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class RouteTimetableEntryResponse {

    private Long id;
    private DayOfWeek serviceDay;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private String notes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

