package multitier.trans.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import multitier.trans.model.enums.StationStatus;

@Entity
@Table(name = "stations", indexes = {
    @Index(name = "idx_station_name", columnList = "name"),
    @Index(name = "idx_station_status", columnList = "status")
})
@Data  // Lombok: Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor  // Required by JPA
@AllArgsConstructor  // Full constructor with all fields (including id)
public class Station {
    
    // Custom constructor for convenience (without id, since it's auto-generated)
    // This is useful for tests and creating new entities before persistence
    public Station(String name, String description, String address, Double latitude, Double longitude, StationStatus status) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Station name cannot be null")
    @Size(min = 2, max = 100, message = "Station name must be between 2 and 100 characters")
    @Column(nullable = false, unique = true)
    private String name;

    @Size(max = 255, message = "Description must be less than 255 characters")
    private String description;

    @NotNull(message = "Address cannot be null")
    @Size(max = 500, message = "Address must be less than 500 characters")
    @Column(name = "address", nullable = false, unique = true)
    private String address;

    @NotNull(message = "Latitude cannot be null")
    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @NotNull(message = "Longitude cannot be null")
    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StationStatus status;
}