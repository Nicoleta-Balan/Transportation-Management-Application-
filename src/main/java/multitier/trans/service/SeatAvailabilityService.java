package multitier.trans.service;

import java.time.LocalDateTime;

/**
 * Service interface for seat availability operations.
 * Replaces database functions calculate_booked_seats(), check_seat_availability(), get_available_seats().
 */
public interface SeatAvailabilityService {

    /**
     * Calculates the number of booked seats for a route and time period.
     * Replaces database function calculate_booked_seats().
     * 
     * @param routeId The route ID
     * @param departureTime The departure time
     * @param arrivalTime The arrival time
     * @return The number of booked seats for overlapping reservations
     */
    int calculateBookedSeats(Long routeId, LocalDateTime departureTime, LocalDateTime arrivalTime);

    /**
     * Checks if the requested number of seats is available for a route and time period.
     * Replaces database function check_seat_availability().
     * 
     * @param routeId The route ID
     * @param requestedSeats The number of seats requested
     * @param departureTime The departure time
     * @param arrivalTime The arrival time
     * @return true if seats are available, false otherwise
     */
    boolean checkSeatAvailability(Long routeId, int requestedSeats, 
                                  LocalDateTime departureTime, LocalDateTime arrivalTime);

    /**
     * Gets the number of available seats for a route and time period.
     * Replaces database function get_available_seats().
     * 
     * @param routeId The route ID
     * @param departureTime The departure time
     * @param arrivalTime The arrival time
     * @return The number of available seats
     */
    int getAvailableSeats(Long routeId, LocalDateTime departureTime, LocalDateTime arrivalTime);
}

