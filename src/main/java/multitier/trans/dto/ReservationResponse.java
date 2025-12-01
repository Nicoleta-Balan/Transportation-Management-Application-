package multitier.trans.dto;

import java.time.LocalDateTime;

public class ReservationResponse {
    private Long id;
    private String passengerName;
    private int seatCount;
    private String status;

    // Details about the Route
    private Long routeId;
    private String routeOrigin;
    private String routeDestination;

    // Trip Details
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;

    public ReservationResponse() {}

    public ReservationResponse(Long id, String passengerName, int seatCount, String status,
                               Long routeId, String routeOrigin, String routeDestination,
                               LocalDateTime departureTime, LocalDateTime arrivalTime) {
        this.id = id;
        this.passengerName = passengerName;
        this.seatCount = seatCount;
        this.status = status;
        this.routeId = routeId;
        this.routeOrigin = routeOrigin;
        this.routeDestination = routeDestination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }

    public int getSeatCount() { return seatCount; }
    public void setSeatCount(int seatCount) { this.seatCount = seatCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }

    public String getRouteOrigin() { return routeOrigin; }
    public void setRouteOrigin(String routeOrigin) { this.routeOrigin = routeOrigin; }

    public String getRouteDestination() { return routeDestination; }
    public void setRouteDestination(String routeDestination) { this.routeDestination = routeDestination; }

    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }

    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }
}