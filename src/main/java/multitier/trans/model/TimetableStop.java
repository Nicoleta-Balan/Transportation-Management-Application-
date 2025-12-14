package multitier.trans.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "timetable_stops",
       uniqueConstraints = @UniqueConstraint(columnNames = {"timetable_id", "sequence_order"}),
       indexes = {
           @Index(name = "idx_timetablestop_timetable_sequence", columnList = "timetable_id, sequence_order")
       })
@Data  // Lombok: Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor  // Required by JPA
public class TimetableStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Timetable cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timetable_id", nullable = false)
    @JsonBackReference("timetable-stops")
    private Timetable timetable;

    @NotNull(message = "Station cannot be null")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @NotNull(message = "Sequence order cannot be null")
    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    @NotNull(message = "Arrival time cannot be null")
    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    @Column(name = "departure_time")
    private LocalDateTime departureTime; // Nullable for end station
}

