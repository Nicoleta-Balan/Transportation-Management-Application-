package multitier.trans.dto;

import java.time.LocalDate;
import java.util.List;

public class RouteTimetableResponse {

    private Long id;
    private Long routeId;
    private String name;
    private String description;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String status;
    private List<RouteTimetableEntryResponse> entries;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<RouteTimetableEntryResponse> getEntries() {
        return entries;
    }

    public void setEntries(List<RouteTimetableEntryResponse> entries) {
        this.entries = entries;
    }
}

