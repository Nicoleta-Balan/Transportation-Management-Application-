package multitier.trans.model;

import jakarta.persistence.*; // New imports
import jakarta.validation.constraints.NotNull;
import multitier.trans.model.enums.VehicleClass;


/**
 * Implements Domain Model Implementation: Station & Route.
 * This entity is now updated to use Station objects instead of simple Strings,
 * creating a proper relational model.
 */

@Entity
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // MODIFICATION: Using a Many-to-One relationship to the Station entity
    @ManyToOne
    @JoinColumn(name = "origin_station_id", nullable = false) // The column name in the DB
    private Station originStation;

    // MODIFICATION: Using a Many-to-One relationship to the Station entity
    @ManyToOne
    @JoinColumn(name = "destination_station_id", nullable = false) // The column name in the DB
    private Station destinationStation;

    @NotNull(message = "Vehicle class cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_class", nullable = false)
    private VehicleClass vehicleClass;
    
    @Column(nullable = false) // Added a constraint
    private int vehicleCapacity;

    // --- Constructors ---

    // Empty constructor required by JPA
    public Route() {
    }

    public Route(Station originStation, Station destinationStation, VehicleClass vehicleClass) {
        this.originStation = originStation;
        this.destinationStation = destinationStation;
        this.vehicleClass = vehicleClass;
        this.vehicleCapacity = vehicleClass != null ? vehicleClass.getSeatCapacity() : 0;
    }
    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Station getOriginStation() {
        return originStation;
    }

    public void setOriginStation(Station originStation) {
        this.originStation = originStation;
    }

    public Station getDestinationStation() {
        return destinationStation;
    }

    public void setDestinationStation(Station destinationStation) {
        this.destinationStation = destinationStation;
    }

    public int getVehicleCapacity() {
        return vehicleCapacity;
    }

    public void setVehicleCapacity(int vehicleCapacity) {
        this.vehicleCapacity = vehicleCapacity;
    }

    public VehicleClass getVehicleClass() {
        return vehicleClass;
    }

    public void setVehicleClass(VehicleClass vehicleClass) {
        this.vehicleClass = vehicleClass;
        // Automatically update capacity when vehicle class is set
        if (vehicleClass != null) {
            this.vehicleCapacity = vehicleClass.getSeatCapacity();
        }
    }
}
