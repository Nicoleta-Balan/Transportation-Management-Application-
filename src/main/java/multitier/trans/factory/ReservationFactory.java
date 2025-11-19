package multitier.trans.factory;

import multitier.trans.dto.CreateReservationRequest;
import multitier.trans.model.Reservation;
import multitier.trans.model.Route;
import multitier.trans.model.TripTimeDetails;
import multitier.trans.model.User;
import org.springframework.stereotype.Component;

/**
 * Factory for creating Reservation entities.
 * 
 * Factory Pattern Benefits:
 * 1. Encapsulates complex reservation creation logic
 * 2. Centralizes default value logic (passenger details from user)
 * 3. Handles TripTimeDetails creation
 * 4. Sets default status and other initialization
 * 5. Makes testing easier (can mock the factory)
 * 6. Separates creation logic from business logic
 * 
 * This factory handles:
 * - Creating Reservation instances
 * - Defaulting passenger details from user if not provided
 * - Creating TripTimeDetails value object
 * - Setting initial status
 * - Associating user and route
 */
@Component
public class ReservationFactory {

    /**
     * Creates a Reservation with the provided information.
     * 
     * @param user The authenticated user making the reservation
     * @param route The route for the reservation
     * @param request The DTO containing reservation details
     * @return A new Reservation instance (not yet persisted)
     */
    public Reservation createReservation(
            User user,
            Route route,
            CreateReservationRequest request) {
        
        // Create the Reservation entity
        Reservation reservation = new Reservation();
        
        // Associate with user and route
        reservation.setUser(user);
        reservation.setRoute(route);
        
        // Set passenger details - if not provided, default to user's details
        String passengerName = determinePassengerName(request.getPassengerName(), user);
        reservation.setPassengerName(passengerName);
        
        // Default email to user's email if not provided
        String passengerEmail = request.getPassengerEmail() != null && 
                               !request.getPassengerEmail().trim().isEmpty()
                ? request.getPassengerEmail()
                : user.getEmail();
        reservation.setPassengerEmail(passengerEmail);
        
        // Default phone to user's phone if not provided
        String passengerPhone = request.getPassengerPhone() != null && 
                               !request.getPassengerPhone().trim().isEmpty()
                ? request.getPassengerPhone()
                : user.getPhone();
        reservation.setPassengerPhone(passengerPhone);
        
        // Set seat count
        reservation.setSeatCount(request.getSeatCount());
        
        // Create and set TripTimeDetails value object
        TripTimeDetails tripDetails = new TripTimeDetails(
                request.getDepartureTime(),
                request.getArrivalTime()
        );
        reservation.setTripDetails(tripDetails);
        
        // Set initial status (default to CONFIRMED)
        reservation.setStatus(multitier.trans.model.enums.ReservationStatus.CONFIRMED);
        
        // Set fare details from the DTO
        reservation.setPassengerCategory(request.getPassengerCategory());
        reservation.setVehicleClass(request.getVehicleClass());
        
        return reservation;
    }

    /**
     * Creates a Reservation with a custom status.
     * Useful for creating PENDING reservations or other statuses.
     * 
     * @param user The authenticated user making the reservation
     * @param route The route for the reservation
     * @param request The DTO containing reservation details
     * @param status The initial status (e.g., "PENDING", "CONFIRMED")
     * @return A new Reservation instance (not yet persisted)
     */
    public Reservation createReservationWithStatus(
            User user,
            Route route,
            CreateReservationRequest request,
            multitier.trans.model.enums.ReservationStatus status) {
        
        Reservation reservation = createReservation(user, route, request);
        reservation.setStatus(status);
        return reservation;
    }

    /**
     * Determines the passenger name, defaulting to user's name if not provided.
     * 
     * @param providedName The name provided in the request (may be null or empty)
     * @param user The user to get default name from
     * @return The passenger name to use
     */
    private String determinePassengerName(String providedName, User user) {
        if (providedName != null && !providedName.trim().isEmpty()) {
            return providedName;
        }
        
        // Default to user's full name
        String fullName = buildFullName(user);
        if (!fullName.isEmpty()) {
            return fullName;
        }
        
        // Fallback to username
        return user.getUsername();
    }

    /**
     * Builds the user's full name from first and last name.
     * 
     * @param user The user
     * @return The full name, or empty string if both are null
     */
    private String buildFullName(User user) {
        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";
        
        String fullName = (firstName + " " + lastName).trim();
        return fullName;
    }
}

