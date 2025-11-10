package multitier.trans.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Represents a single timetable entry for a specific day of the week.
 */
@Entity
@Table(name = "route_timetable_entries")
public class RouteTimetableEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timetable_id", nullable = false)
    @JsonIgnore
    private RouteTimetable timetable;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_day", nullable = false, length = 10)
    private DayOfWeek serviceDay;

    @Column(name = "departure_time", nullable = false)
    private LocalTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    private LocalTime arrivalTime;

    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public RouteTimetableEntry() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RouteTimetable getTimetable() {
        return timetable;
    }

    public void setTimetable(RouteTimetable timetable) {
        this.timetable = timetable;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

