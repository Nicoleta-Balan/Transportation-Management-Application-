package multitier.trans.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "route_stops", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"route_id", "sequence_order"}),
       indexes = {
           @Index(name = "idx_routestop_route_sequence", columnList = "route_id, sequence_order")
       })
@Data  // Lombok: Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor  // Required by JPA
@AllArgsConstructor  // Replaces custom constructor
public class RouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Route cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    @JsonBackReference("route-stops")
    private Route route;

    @NotNull(message = "Station cannot be null")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @NotNull(message = "Sequence order cannot be null")
    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder; // 0 for start, 1, 2, 3... for intermediary, last for end

    @NotNull(message = "Distance from previous cannot be null")
    @Column(name = "distance_from_previous", nullable = false)
    private Double distanceFromPrevious; // Distance from previous stop in km

    @NotNull(message = "Duration from previous cannot be null")
    @Column(name = "duration_minutes_from_previous", nullable = false)
    private Integer durationMinutesFromPrevious; // Duration from previous stop in minutes

    @NotNull(message = "Cumulative distance cannot be null")
    @Column(name = "cumulative_distance", nullable = false)
    private Double cumulativeDistance; // Total distance from start to this stop

    @NotNull(message = "Cumulative duration cannot be null")
    @Column(name = "cumulative_duration_minutes", nullable = false)
    private Integer cumulativeDurationMinutes; // Total duration from start to this stop
}

