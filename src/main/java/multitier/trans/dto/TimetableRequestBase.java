package multitier.trans.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data  // Lombok: Generates getters, setters, toString, equals, hashCode for standard fields
public abstract class TimetableRequestBase {

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    private List<String> daysOfWeek;

    @Valid
    protected List<TimetableStopRequest> stops;
}

