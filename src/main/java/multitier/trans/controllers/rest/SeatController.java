package multitier.trans.controllers.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import multitier.trans.dto.seat.OccupiedSeatsResponse;
import multitier.trans.dto.seat.SeatHoldRequest;
import multitier.trans.dto.seat.SeatHoldResponse;
import multitier.trans.service.SeatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Tag(name = "Seats", description = "API for managing seat availability and holds")
@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @Operation(summary = "Get occupied and held seats for a route/departure")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Seat availability retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    @GetMapping("/availability")
    public ResponseEntity<OccupiedSeatsResponse> getOccupiedSeats(
            @Parameter(description = "Route ID", required = true)
            @RequestParam Long routeId,
            @Parameter(description = "Departure time (ISO format)", required = true)
            @RequestParam String departureTime,
            @Parameter(description = "Session ID for identifying user's held seats", required = true)
            @RequestParam String sessionId) {

        LocalDateTime departure = parseDateTime(departureTime);
        OccupiedSeatsResponse response = seatService.getOccupiedSeats(routeId, departure, sessionId);
        return ResponseEntity.ok(response);
    }

    /**
     * Parse datetime string handling both ISO format with 'Z' suffix and plain LocalDateTime format.
     * Converts UTC timezone to system default timezone for consistent storage.
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            // Try parsing as ISO instant (e.g., "2026-01-20T21:51:00.000Z")
            if (dateTimeStr.endsWith("Z") || dateTimeStr.contains("+") || dateTimeStr.contains("-T")) {
                Instant instant = Instant.parse(dateTimeStr);
                return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            }
        } catch (DateTimeParseException e) {
            // Fall through to try LocalDateTime parsing
        }

        // Try parsing as LocalDateTime (e.g., "2026-01-20T21:51:00")
        return LocalDateTime.parse(dateTimeStr);
    }

    @Operation(summary = "Hold seats temporarily during booking process")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Seats held successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or seats unavailable"),
        @ApiResponse(responseCode = "404", description = "Route not found")
    })
    @PostMapping("/hold")
    public ResponseEntity<SeatHoldResponse> holdSeats(
            @Valid @RequestBody SeatHoldRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userEmail = userDetails != null ? userDetails.getUsername() : null;
        SeatHoldResponse response = seatService.holdSeats(request, userEmail);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Release held seats (e.g., when user leaves booking page)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Seats released successfully")
    })
    @DeleteMapping("/hold/{sessionId}")
    public ResponseEntity<Void> releaseHolds(
            @Parameter(description = "Session ID", required = true)
            @PathVariable String sessionId) {

        seatService.releaseHolds(sessionId);
        return ResponseEntity.ok().build();
    }
}
