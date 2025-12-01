package multitier.trans.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import multitier.trans.dto.CreateReservationRequest;
import multitier.trans.model.*; // Import everything from the model package
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

    @Test
    public void whenCreateReservation_withValidData_thenReturns201Created() throws Exception {
        CreateReservationRequest request = new CreateReservationRequest();
        request.setRouteId(1L);
        request.setPassengerName("Test Passenger");
        request.setSeatCount(2);
        request.setDepartureTime(testDeparture);
        request.setArrivalTime(testArrival);
        request.setPassengerCategory(PassengerCategory.ADULT);
        request.setVehicleClass(VehicleClass.SECOND_CLASS); // Corrected from STANDARD

        Reservation savedReservation = new Reservation();
        savedReservation.setId(1L);
        savedReservation.setRoute(testRoute);
        savedReservation.setPassengerName("Test Passenger");
        savedReservation.setSeatCount(2);
        savedReservation.setStatus("CONFIRMED");
        savedReservation.setTripDetails(new TripTimeDetails(testDeparture, testArrival));
        savedReservation.setPassengerCategory(PassengerCategory.ADULT);
        savedReservation.setVehicleClass(VehicleClass.SECOND_CLASS); // Corrected from STANDARD

        when(reservationService.createReservation(any(CreateReservationRequest.class))).thenReturn(savedReservation);

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.passengerName").value("Test Passenger"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    public void whenCancelReservation_withValidId_thenReturns200OK() throws Exception {
        Reservation cancelledReservation = new Reservation();
        cancelledReservation.setId(1L);
        cancelledReservation.setRoute(testRoute);
        cancelledReservation.setPassengerName("Test Passenger");
        cancelledReservation.setStatus("CANCELLED");
        cancelledReservation.setPassengerCategory(PassengerCategory.ADULT);
        cancelledReservation.setVehicleClass(VehicleClass.SECOND_CLASS); // Corrected from STANDARD

        when(reservationService.cancelReservation(1L)).thenReturn(cancelledReservation);

        mockMvc.perform(put("/api/reservations/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    public void whenCreateReservation_withInvalidData_thenReturns400BadRequest() throws Exception {
        CreateReservationRequest request = new CreateReservationRequest();
        request.setRouteId(1L);
        request.setPassengerName(""); // Invalid
        request.setSeatCount(2);
        request.setDepartureTime(testDeparture);
        request.setArrivalTime(testArrival);
        request.setPassengerCategory(PassengerCategory.ADULT);
        request.setVehicleClass(VehicleClass.SECOND_CLASS); // Corrected from STANDARD

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}