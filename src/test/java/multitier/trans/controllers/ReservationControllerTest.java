package multitier.trans.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import multitier.trans.dto.CreateReservationRequest;
import multitier.trans.model.Reservation;
import multitier.trans.model.Route;
import multitier.trans.model.Station;
import multitier.trans.model.TripTimeDetails;
import multitier.trans.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

// Import methods for testing
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Implements Junit Test for Reservation/Cancellation Service.
 *
 * This test validates the ReservationController, ensuring that:
 * 1. Valid reservations can be created
 * 2. Reservations can be cancelled
 * 3. Invalid data (like a missing passenger name) is rejected.
 */

@WebMvcTest(ReservationController.class) // Tell Spring to only test the Controller layer
public class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc; // A tool to fake/simulate HTTP requests

    @Autowired
    private ObjectMapper objectMapper; // A tool to convert Java objects to JSON strings

    @MockBean // Creates a "fake" version of the service "brain"
    private ReservationService reservationService;

    // Re-usable test objects
    private Station testStationA;
    private Station testStationB;
    private Route testRoute;
    private LocalDateTime testDeparture;
    private LocalDateTime testArrival;

    @BeforeEach
    void setUp() {
        // This method runs before each @Test, setting up clean data
        testStationA = new Station("Origin", "Desc A", "Active");
        testStationA.setId(1L);

        testStationB = new Station("Destination", "Desc B", "Active");
        testStationB.setId(2L);

        testRoute = new Route(testStationA, testStationB, 50);
        testRoute.setId(1L);

        testDeparture = LocalDateTime.of(2025, 11, 20, 10, 0);
        testArrival = LocalDateTime.of(2025, 11, 20, 12, 0);
    }

    /**
     * Test for Reservation Creation (Happy Path)
     */
    @Test
    public void whenCreateReservation_withValidData_thenReturns201Created() throws Exception {
        // 1. Arrange (Set up the test)
        // This is the DTO (form) the client will send
        CreateReservationRequest request = new CreateReservationRequest();
        request.setRouteId(1L);
        request.setPassengerName("Test Passenger");
        request.setSeatCount(2);
        request.setDepartureTime(testDeparture);
        request.setArrivalTime(testArrival);

        // This is the full Reservation object we expect the service to return
        Reservation savedReservation = new Reservation();
        savedReservation.setId(1L); // The DB assigns an ID
        savedReservation.setRoute(testRoute);
        savedReservation.setPassengerName("Test Passenger");
        savedReservation.setSeatCount(2);
        savedReservation.setStatus("CONFIRMED");
        savedReservation.setTripDetails(new TripTimeDetails(testDeparture, testArrival));

        // Tell the "fake" service what to do:
        // "WHEN you receive ANY CreateReservationRequest, THEN return our 'savedReservation'"
        when(reservationService.createReservation(any(CreateReservationRequest.class))).thenReturn(savedReservation);

        // 2. Act (Perform the action) & 3. Assert (Check the result)
        mockMvc.perform(post("/api/reservations") // Fake a POST request
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))) // Send the request DTO as JSON
                .andExpect(status().isCreated()) // We expect a 201 Created status
                .andExpect(jsonPath("$.id").value(1L)) // Check that the returned JSON has the ID
                .andExpect(jsonPath("$.passengerName").value("Test Passenger"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    /**
     * Test for SCRUM-27: Reservation Cancellation
     */
    @Test
    public void whenCancelReservation_withValidId_thenReturns200OK() throws Exception {
        // 1. Arrange
        // This is the object the service will return
        Reservation cancelledReservation = new Reservation();
        cancelledReservation.setId(1L);
        cancelledReservation.setRoute(testRoute);
        cancelledReservation.setPassengerName("Test Passenger");
        cancelledReservation.setStatus("CANCELLED"); // The service changed the status

        // Tell the "fake" service:
        // "WHEN you are asked to cancel reservation 1, THEN return this 'cancelledReservation'"
        when(reservationService.cancelReservation(1L)).thenReturn(cancelledReservation);

        // 2. Act & 3. Assert
        mockMvc.perform(put("/api/reservations/1/cancel")) // Fake a PUT request to the cancel URL
                .andExpect(status().isOk()) // We expect a 200 OK status
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("CANCELLED")); // Check that the status is now CANCELLED
    }

    /**
     * Test for Validation (part of SCRUM-29)
     * Checks if the API rejects a request with invalid data (e.g., blank passenger name)
     */
    @Test
    public void whenCreateReservation_withInvalidData_thenReturns400BadRequest() throws Exception {
        // 1. Arrange
        // This request is invalid because passengerName is blank (our entity has @NotBlank)
        CreateReservationRequest request = new CreateReservationRequest();
        request.setRouteId(1L);
        request.setPassengerName(""); // <-- INVALID
        request.setSeatCount(2);
        request.setDepartureTime(testDeparture);
        request.setArrivalTime(testArrival);

        // 2. Act & 3. Assert
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // We expect a 400 Bad Request
    }
}