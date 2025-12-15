package multitier.trans.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import multitier.trans.model.enums.VehicleClass;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "routes", indexes = {
    @Index(name = "idx_route_vehicle_class", columnList = "vehicle_class")
})
@Getter  // Lombok: Generates getters for all fields
@Setter  // Lombok: Generates setters for simple fields (custom setters override these)
@NoArgsConstructor  // Required by JPA
// Using @Getter/@Setter instead of @Data to avoid equals/hashCode issues with lazy @OneToMany relationships
// Custom setters (setVehicleClass, setRouteStops) are kept for business logic
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Route stops - ordered list of stations on this route
    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sequenceOrder ASC")
    @JsonManagedReference("route-stops")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private List<RouteStop> routeStops = new ArrayList<>();

    @NotNull(message = "Vehicle class cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_class", nullable = false)
    private VehicleClass vehicleClass;
    
    @Column(nullable = false)
    private int vehicleCapacity;

    @NotNull(message = "Distance cannot be null")
    @Column(name = "distance", nullable = false)
    private Double distance; // in km

    @NotNull(message = "Duration cannot be null")
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes; // in minutes

    @Size(max = 50, message = "Description must not exceed 50 characters")
    @Column(name = "description")
    private String description;

    // Helper methods to get origin and destination from routeStops
    // These maintain backward compatibility with existing code
    @JsonIgnore  // Ignore when Route is embedded in other entities to prevent lazy loading
    public Station getOriginStation() {
        if (routeStops == null || routeStops.isEmpty()) {
            return null;
        }
        return routeStops.get(0).getStation();
    }

    @JsonIgnore  // Ignore when Route is embedded in other entities to prevent lazy loading
    public Station getDestinationStation() {
        if (routeStops == null || routeStops.isEmpty()) {
            return null;
        }
        return routeStops.get(routeStops.size() - 1).getStation();
    }
    
    // Get all intermediary stations (excluding first and last)
    @JsonIgnore  // Ignore when Route is embedded in other entities to prevent lazy loading
    public List<Station> getIntermediaryStations() {
        if (routeStops == null || routeStops.size() <= 2) {
            return Collections.emptyList();
        }
        return routeStops.subList(1, routeStops.size() - 1)
            .stream()
            .map(RouteStop::getStation)
            .collect(Collectors.toList());
    }
    
    // Custom setter with business logic: automatically updates capacity when vehicle class is set
    public void setVehicleClass(VehicleClass vehicleClass) {
        this.vehicleClass = vehicleClass;
        if (vehicleClass != null) {
            this.vehicleCapacity = vehicleClass.getSeatCapacity();
        }
    }
    
    // Custom setter with business logic: manages collection properly with orphanRemoval
    public void setRouteStops(List<RouteStop> routeStops) {
        // Only clear if collection is not empty (for updates)
        // For new entities, clearing an empty collection can interfere with JPA tracking
        if (!this.routeStops.isEmpty()) {
            this.routeStops.clear();
        }
        // Add new stops and ensure bidirectional relationship is maintained
        if (routeStops != null) {
            for (RouteStop stop : routeStops) {
                // Ensure the bidirectional relationship is set
                if (stop.getRoute() != this) {
                    stop.setRoute(this);
                }
                this.routeStops.add(stop);
            }
        }
    }
}
