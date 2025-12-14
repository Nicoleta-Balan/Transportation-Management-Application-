package multitier.trans.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Embeddable
@Data  // Lombok: Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor  // Required by JPA for embeddables
@AllArgsConstructor  // Convenience constructor for creating instances
public class TripTimeDetails {

    @NotNull(message = "Departure time cannot be null")
    private LocalDateTime departureTime;

    @NotNull(message = "Arrival time cannot be null")
    private LocalDateTime arrivalTime;
}