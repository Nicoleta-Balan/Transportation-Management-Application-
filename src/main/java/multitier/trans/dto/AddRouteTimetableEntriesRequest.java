package multitier.trans.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class AddRouteTimetableEntriesRequest {

    @NotEmpty(message = "At least one timetable entry must be provided")
    @Valid
    private List<RouteTimetableEntryRequest> entries;

    public List<RouteTimetableEntryRequest> getEntries() {
        return entries;
    }

    public void setEntries(List<RouteTimetableEntryRequest> entries) {
        this.entries = entries;
    }
}

