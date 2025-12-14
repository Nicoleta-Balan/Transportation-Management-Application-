package multitier.trans.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "timetables", indexes = {
    @Index(name = "idx_timetable_route", columnList = "route_id")
})
@Getter  // Lombok: Generates getters
@Setter  // Lombok: Generates setters
@NoArgsConstructor  // Required by JPA
// Note: Using @Getter/@Setter instead of @Data to avoid equals/hashCode issues with lazy @OneToMany relationships
public class Timetable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Route cannot be null")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "route_id", nullable = false)
    @JsonIgnoreProperties(value = {"routeStops", "hibernateLazyInitializer", "handler"}, allowSetters = false)
    private Route route;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    @Column(name = "description")
    private String description;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "timetable_days", joinColumns = @JoinColumn(name = "timetable_id"))
    @Column(name = "day_of_week")
    private List<String> daysOfWeek = new ArrayList<>();

    @OneToMany(mappedBy = "timetable", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sequenceOrder ASC")
    @JsonManagedReference("timetable-stops")
    private List<TimetableStop> timetableStops = new ArrayList<>();
    
    // Custom setter to ensure bidirectional relationship is maintained
    public void setTimetableStops(List<TimetableStop> timetableStops) {
        this.timetableStops.clear();
        if (timetableStops != null) {
            for (TimetableStop stop : timetableStops) {
                // Ensure the bidirectional relationship is set
                if (stop.getTimetable() != this) {
                    stop.setTimetable(this);
                }
                this.timetableStops.add(stop);
            }
        }
    }
}

