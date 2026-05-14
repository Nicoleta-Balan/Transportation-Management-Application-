package multitier.trans.dto.seat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SeatHoldRequest {

    @NotNull(message = "Route ID cannot be null")
    private Long routeId;

    @NotNull(message = "Departure time cannot be null")
    private LocalDateTime departureTime;

    private List<String> seatNumbers;
    
    private int seatCount;

    @NotBlank(message = "Session ID cannot be blank")
    private String sessionId;
}
