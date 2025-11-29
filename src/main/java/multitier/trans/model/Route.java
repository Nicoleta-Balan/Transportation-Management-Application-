package multitier.trans.model;
import jakarta.persistence.*;

@Entity
@Table(name = "routes") // table name in the DB, marked class as JPA entity
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // represents the primary key
    private Long id;

    @ManyToOne // relation between routes and stations - origin station
    @JoinColumn(name = "origin_station_id", nullable = false)
    private Station originStation;


    @ManyToOne // relation between routes and stations - destination station
    @JoinColumn(name = "destination_station_id", nullable = false)
    private Station destinationStation;

    @Column(nullable = false) // null values are not allowed
    private int vehicleCapacity;

    public Route() {
    }

    // Constructor to use Station objects
    public Route(Station originStation, Station destinationStation, int vehicleCapacity) {
        this.originStation = originStation;
        this.destinationStation = destinationStation;
        this.vehicleCapacity = vehicleCapacity;
    }

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
