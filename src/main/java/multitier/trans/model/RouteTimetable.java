package multitier.trans.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import multitier.trans.model.enums.TimetableStatus;
import multitier.trans.model.converter.TimetableStatusConverter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a named timetable that groups multiple recurring trip entries for a route.
 * Extends BaseEntity for automatic timestamp management (createdAt, updatedAt).
 */
@Entity
@Table(name = "route_timetables")
public class RouteTimetable extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    @JsonIgnore
    private Route route;

    @Column(nullable = false, length = 100)
    private String name;

    private String description;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @NotNull(message = "Status cannot be null")
    @Convert(converter = TimetableStatusConverter.class)
    @Column(nullable = false, length = 20)
    private TimetableStatus status = TimetableStatus.ACTIVE;

    @OneToMany(mappedBy = "timetable", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RouteTimetableEntry> entries = new ArrayList<>();

    public RouteTimetable() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public TimetableStatus getStatus() {
        return status;
    }

    public void setStatus(TimetableStatus status) {
        this.status = status;
    }

    public List<RouteTimetableEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<RouteTimetableEntry> entries) {
        this.entries = entries;
    }

    public void addEntry(RouteTimetableEntry entry) {
        entries.add(entry);
        entry.setTimetable(this);
    }
}

