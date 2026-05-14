package multitier.trans.dto.seat;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SeatHoldResponse {
    private boolean success;
    private String message;
    private List<String> heldSeats;
    private List<String> failedSeats;
    private LocalDateTime expiresAt;
}
