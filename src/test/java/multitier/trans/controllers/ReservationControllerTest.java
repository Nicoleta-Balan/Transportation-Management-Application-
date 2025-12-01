package multitier.trans.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import multitier.trans.dto.CreateReservationRequest;
import multitier.trans.model.Reservation;
import multitier.trans.model.Route;
import multitier.trans.model.Station;
import multitier.trans.model.TripTimeDetails;
import multitier.trans.model.enums.PassengerCategory;
import multitier.trans.model.enums.VehicleClass;
import multitier.trans.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired; //used to inject dependencies into the test class
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean; // configures false versions of the service
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc; //used to simulate HTTP requests

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any; // to define false services
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ReservationController.class) // Testing the Controller layer
public class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc; // to simulate HTTP requests

    @Autowired
    private ObjectMapper objectMapper; // To convert Java objects to JSON strings

    @MockBean
    private ReservationService reservationService;

    private Station testStationA;
    private Station testStationB;
    private Route testRoute;
    private LocalDateTime testDeparture;
    private LocalDateTime testArrival;

    @BeforeEach
    void setUp() {
        testStationA = new Station("Origin", "Desc A", "Active");
        testStationA.setId(1L);

        testStationB = new Station("Destination", "Desc B", "Active");
        testStationB.setId(2L);

        testRoute = new Route(testStationA, testStationB, 50);
        testRoute.setId(1L);

        testDeparture = LocalDateTime.of(2025, 11, 20, 10, 0);
        testArrival = LocalDateTime.of(2025, 11, 20, 12, 0);
    }




    //Test for Happy Path - Reservation Creation

    @Test
    public void whenCreateReservation_withValidData_thenReturns201Created() throws Exception {
        // This is the DTO (form) the client will send
        CreateReservationRequest request = new CreateReservationRequest();
        request.setRouteId(1L);
        request.setPassengerName("Test Passenger");
        request.setSeatCount(2);
        request.setDepartureTime(testDeparture);
        request.setArrivalTime(testArrival);

        request.setPassengerCategory(PassengerCategory.ADULT);
        request.setVehicleClass(VehicleClass.STANDARD);


        Reservation savedReservation = new Reservation();
        savedReservation.setId(1L);
        savedReservation.setRoute(testRoute);
        savedReservation.setPassengerName("Test Passenger");
        savedReservation.setSeatCount(2);
        savedReservation.setStatus("CONFIRMED");
        savedReservation.setTripDetails(new TripTimeDetails(testDeparture, testArrival));
        savedReservation.setPassengerCategory(PassengerCategory.ADULT);
        savedReservation.setVehicleClass(VehicleClass.STANDARD);

        // Tell the fake service: "WHEN you receive this request, THEN return this Reservation"
        when(reservationService.createReservation(any(CreateReservationRequest.class))).thenReturn(savedReservation);

        mockMvc.perform(post("/api/reservations") // Fake a POST request
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))) // Send the request DTO as JSON otherwise the controller cannot process it
                .andExpect(status().isCreated()) // We expect a 201 Created status
                .andExpect(jsonPath("$.id").value(1L)) // Check that the returned JSON has the ID
                .andExpect(jsonPath("$.passengerName").value("Test Passenger"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }




//Test for Reservation Cancellation

    @Test
    public void whenCancelReservation_withValidId_thenReturns200OK() throws Exception {
        // This is the object the service will return
        Reservation cancelledReservation = new Reservation();
        cancelledReservation.setId(1L);
        cancelledReservation.setRoute(testRoute);
        cancelledReservation.setPassengerName("Test Passenger");
        cancelledReservation.setStatus("CANCELLED"); // The service changed the status
        cancelledReservation.setPassengerCategory(PassengerCategory.ADULT);
        cancelledReservation.setVehicleClass(VehicleClass.STANDARD);

        // tell the fake service: "WHEN you receive this request, THEN return this Reservation"
        when(reservationService.cancelReservation(1L)).thenReturn(cancelledReservation);

        mockMvc.perform(put("/api/reservations/1/cancel")) // Fake a PUT request to the cancel URL
                .andExpect(status().isOk()) // We expect a 200 OK status
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }




// Test for Validation that checks if the API rejects a request with invalid data (ex. blank passenger name)


    @Test
    public void whenCreateReservation_withInvalidData_thenReturns400BadRequest() throws Exception {
        // This request is invalid because passengerName is blank (our entity has @NotBlank)
        CreateReservationRequest request = new CreateReservationRequest();
        request.setRouteId(1L);
        request.setPassengerName(""); // <-- INVALID
        request.setSeatCount(2);
        request.setDepartureTime(testDeparture);
        request.setArrivalTime(testArrival);
        request.setPassengerCategory(PassengerCategory.ADULT);
        request.setVehicleClass(VehicleClass.STANDARD);

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Expecting 400 Bad Request
    }
}