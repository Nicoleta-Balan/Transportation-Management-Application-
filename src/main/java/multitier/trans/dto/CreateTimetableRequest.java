package multitier.trans.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data  // Lombok: Generates getters, setters, toString, equals, hashCode
@EqualsAndHashCode(callSuper = true)  // Include parent fields in equals/hashCode
public class CreateTimetableRequest extends TimetableRequestBase {

    @NotNull(message = "Route ID cannot be null")
    private Long routeId;

    // Override stops field to add @NotNull validation for create
    @NotNull(message = "Timetable stops cannot be null")
    @Valid
    private List<TimetableStopRequest> stops;
}

