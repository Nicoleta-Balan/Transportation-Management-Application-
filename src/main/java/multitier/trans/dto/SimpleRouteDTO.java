package multitier.trans.dto;

public class SimpleRouteDTO {
    private Long id;
    private String originStation;
    private String destinationStation;
    private String vehicleClass;
    private Double distance;
    private Integer durationMinutes;

    public SimpleRouteDTO() {}

    public SimpleRouteDTO(Long id, String originStation, String destinationStation,
                          String vehicleClass, Double distance, Integer durationMinutes) {
        this.id = id;
        this.originStation = originStation;
        this.destinationStation = destinationStation;
        this.vehicleClass = vehicleClass;
        this.distance = distance;
        this.durationMinutes = durationMinutes;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOriginStation() { return originStation; }
    public void setOriginStation(String originStation) { this.originStation = originStation; }

    public String getDestinationStation() { return destinationStation; }
    public void setDestinationStation(String destinationStation) { this.destinationStation = destinationStation; }

    public String getVehicleClass() { return vehicleClass; }
    public void setVehicleClass(String vehicleClass) { this.vehicleClass = vehicleClass; }

    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
}