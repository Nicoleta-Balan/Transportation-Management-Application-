package multitier.trans.dto.seat;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OccupiedSeatsResponse {
    private Long routeId;
    private String departureTime;
    private List<String> occupiedSeats;  // Seats from confirmed reservations
    private List<String> heldSeats;      // Seats temporarily held by other users
    private List<String> myHeldSeats;    // Seats held by the current session
}
