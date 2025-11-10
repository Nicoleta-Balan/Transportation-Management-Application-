package multitier.trans.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity mapped to the daily/monthly revenue summary denormalized table.
 */
@Entity
@Table(name = "revenue_summary")
public class RevenueSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "summary_date", nullable = false)
    private LocalDate summaryDate;

    @Column(name = "route_id")
    private Long routeId;

    @Column(name = "passenger_category")
    private String passengerCategory;

    @Column(name = "vehicle_class")
    private String vehicleClass;

    @Column(name = "reservation_count")
    private Integer reservationCount;

    @Column(name = "total_revenue")
    private BigDecimal totalRevenue;

    @Column(name = "total_vat")
    private BigDecimal totalVat;

    @Column(name = "net_revenue")
    private BigDecimal netRevenue;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    public RevenueSummary() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getSummaryDate() {
        return summaryDate;
    }

    public void setSummaryDate(LocalDate summaryDate) {
        this.summaryDate = summaryDate;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public String getPassengerCategory() {
        return passengerCategory;
    }

    public void setPassengerCategory(String passengerCategory) {
        this.passengerCategory = passengerCategory;
    }

    public String getVehicleClass() {
        return vehicleClass;
    }

    public void setVehicleClass(String vehicleClass) {
        this.vehicleClass = vehicleClass;
    }

    public Integer getReservationCount() {
        return reservationCount;
    }

    public void setReservationCount(Integer reservationCount) {
        this.reservationCount = reservationCount;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getTotalVat() {
        return totalVat;
    }

    public void setTotalVat(BigDecimal totalVat) {
        this.totalVat = totalVat;
    }

    public BigDecimal getNetRevenue() {
        return netRevenue;
    }

    public void setNetRevenue(BigDecimal netRevenue) {
        this.netRevenue = netRevenue;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }
}

