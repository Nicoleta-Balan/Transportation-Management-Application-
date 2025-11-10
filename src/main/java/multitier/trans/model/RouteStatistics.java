package multitier.trans.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity mapped to the denormalized table with aggregated statistics per route.
 */
@Entity
@Table(name = "route_statistics")
public class RouteStatistics {

    @Id
    @Column(name = "route_id")
    private Long routeId;

    @Column(name = "total_reservations")
    private Integer totalReservations;

    @Column(name = "confirmed_reservations")
    private Integer confirmedReservations;

    @Column(name = "cancelled_reservations")
    private Integer cancelledReservations;

    @Column(name = "total_revenue")
    private BigDecimal totalRevenue;

    @Column(name = "average_occupancy_rate")
    private BigDecimal averageOccupancyRate;

    @Column(name = "last_calculated")
    private LocalDateTime lastCalculated;

    public RouteStatistics() {
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public Integer getTotalReservations() {
        return totalReservations;
    }

    public void setTotalReservations(Integer totalReservations) {
        this.totalReservations = totalReservations;
    }

    public Integer getConfirmedReservations() {
        return confirmedReservations;
    }

    public void setConfirmedReservations(Integer confirmedReservations) {
        this.confirmedReservations = confirmedReservations;
    }

    public Integer getCancelledReservations() {
        return cancelledReservations;
    }

    public void setCancelledReservations(Integer cancelledReservations) {
        this.cancelledReservations = cancelledReservations;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getAverageOccupancyRate() {
        return averageOccupancyRate;
    }

    public void setAverageOccupancyRate(BigDecimal averageOccupancyRate) {
        this.averageOccupancyRate = averageOccupancyRate;
    }

    public LocalDateTime getLastCalculated() {
        return lastCalculated;
    }

    public void setLastCalculated(LocalDateTime lastCalculated) {
        this.lastCalculated = lastCalculated;
    }
}

