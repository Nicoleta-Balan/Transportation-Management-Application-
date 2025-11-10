package multitier.trans.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entity mapped to the denormalized table that stores the latest seat availability per route.
 */
@Entity
@Table(name = "route_availability")
public class RouteAvailability {

    @Id
    @Column(name = "route_id")
    private Long routeId;

    @Column(name = "total_capacity", nullable = false)
    private Integer totalCapacity;

    @Column(name = "booked_seats", nullable = false)
    private Integer bookedSeats;

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    public RouteAvailability() {
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public Integer getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(Integer totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public Integer getBookedSeats() {
        return bookedSeats;
    }

    public void setBookedSeats(Integer bookedSeats) {
        this.bookedSeats = bookedSeats;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}

