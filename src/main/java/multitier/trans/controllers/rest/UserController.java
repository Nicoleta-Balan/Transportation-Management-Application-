package multitier.trans.controllers.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import multitier.trans.dto.ReservationResponse;
import multitier.trans.service.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User", description = "API for user-specific operations")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final ReservationService reservationService;

    @Operation(summary = "Get user's active bookings")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active bookings retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/bookings/active")
    public ResponseEntity<List<ReservationResponse>> getActiveBookings(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ReservationResponse> bookings = reservationService.getActiveReservationsForUser(
            userDetails.getUsername()
        );
        return ResponseEntity.ok(bookings);
    }

    @Operation(summary = "Get user's past bookings (ticket history)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Past bookings retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/bookings/history")
    public ResponseEntity<List<ReservationResponse>> getBookingHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ReservationResponse> bookings = reservationService.getPastReservationsForUser(
            userDetails.getUsername()
        );
        return ResponseEntity.ok(bookings);
    }
}
