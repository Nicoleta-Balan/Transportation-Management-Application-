package multitier.trans.dto;

import lombok.Builder;
import lombok.Data;
import multitier.trans.model.enums.PassengerCategory;
import multitier.trans.model.enums.ReservationStatus;
import multitier.trans.model.enums.VehicleClass;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ReservationResponse {
    private Long id;
    private Long routeId;
    private String originStation;
    private String destinationStation;
    private String passengerName;
    private int seatCount;
    private String selectedSeats;
    private ReservationStatus status;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private PassengerCategory passengerCategory;
    private VehicleClass vehicleClass;
    private BigDecimal totalPrice;
    private String currency;
    private LocalDateTime createdAt;
    private String qrCode;
}
