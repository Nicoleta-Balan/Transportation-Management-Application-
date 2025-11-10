package multitier.trans.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CreateRouteTimetableRequest {

    @NotBlank(message = "Timetable name is required")
    private String name;

    private String description;

    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    private String status = "ACTIVE";

    @Valid
    private List<RouteTimetableEntryRequest> entries = new ArrayList<>();

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

    public List<RouteTimetableEntryRequest> getEntries() {
        return entries;
    }

    public void setEntries(List<RouteTimetableEntryRequest> entries) {
        this.entries = entries;
    }
}

