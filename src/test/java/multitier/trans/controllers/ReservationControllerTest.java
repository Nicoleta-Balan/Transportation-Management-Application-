package multitier.trans.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import multitier.trans.dto.CreateReservationRequest;
import multitier.trans.model.Reservation;
import multitier.trans.model.Route;
import multitier.trans.model.Station;
import multitier.trans.model.TripTimeDetails;
import multitier.trans.model.enums.PassengerCategory;
import multitier.trans.model.enums.ReservationStatus;
import multitier.trans.model.enums.StationStatus;
import multitier.trans.model.enums.VehicleClass;
import multitier.trans.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
public class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservationService reservationService;

    private Route testRoute;
    private LocalDateTime testDeparture;
    private LocalDateTime testArrival;

    @BeforeEach
    void setUp() {
        Station testStationA = new Station("Origin", "Desc A", "address", 47.1788, 27.56716, StationStatus.ACTIVE);
        testStationA.setId(1L);

        Station testStationB = new Station("Destination", "Desc B", "address", 47.1788, 27.56716, StationStatus.ACTIVE);
        testStationB.setId(2L);

        testRoute = new Route(testStationA, testStationB, VehicleClass.STANDARD);
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
        // This is the DTO (form) that the client sends

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
        savedReservation.setStatus(ReservationStatus.CONFIRMED);
        savedReservation.setTripDetails(new TripTimeDetails(testDeparture, testArrival));
        savedReservation.setPassengerCategory(PassengerCategory.ADULT);
        savedReservation.setVehicleClass(VehicleClass.STANDARD);

        // We tell the service what to do
        when(reservationService.createReservation(any(CreateReservationRequest.class))).thenReturn(savedReservation);

        // 2. Act (Perform the action) & 3. Assert (Check the result)
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))) // We send the DTO as a JSON
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.passengerName").value("Test Passenger"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    /**
     * Test for Reservation Cancellation
     */

    @Test
    public void whenCancelReservation_withValidId_thenReturns200OK() throws Exception {
        // 1. Arrange
        Reservation cancelledReservation = new Reservation();
        cancelledReservation.setId(1L);
        cancelledReservation.setRoute(testRoute);
        cancelledReservation.setPassengerName("Test Passenger");
        cancelledReservation.setStatus(ReservationStatus.CANCELLED); // The service changed the status
        cancelledReservation.setPassengerCategory(PassengerCategory.ADULT);
        cancelledReservation.setVehicleClass(VehicleClass.STANDARD);

        when(reservationService.cancelReservation(1L)).thenReturn(cancelledReservation);

        // 2. Act & 3. Assert
        mockMvc.perform(put("/api/reservations/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    /**
     * Test for validation
     */

    @Test
    public void whenCreateReservation_withInvalidData_thenReturns400BadRequest() throws Exception {
        // 1. Arrange
        CreateReservationRequest request = new CreateReservationRequest();
        request.setRouteId(1L);
        request.setPassengerName(""); // <-- INVALID
        request.setSeatCount(2);
        request.setDepartureTime(testDeparture);
        request.setArrivalTime(testArrival);
        request.setPassengerCategory(PassengerCategory.ADULT);
        request.setVehicleClass(VehicleClass.STANDARD);

        // 2. Act & 3. Assert
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Expecting 400 Bad Request
    }
}