package multitier.trans.model;

import jakarta.persistence.*; // New imports


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

    @Column(nullable = false) // Added a constraint
    private int vehicleCapacity;

    // --- Constructors ---

    // Empty constructor required by JPA
    public Route() {
    }

    // Constructor to use Station objects
    public Route(Station originStation, Station destinationStation, int vehicleCapacity) {
        this.originStation = originStation;
        this.destinationStation = destinationStation;
        this.vehicleCapacity = vehicleCapacity;
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
}
