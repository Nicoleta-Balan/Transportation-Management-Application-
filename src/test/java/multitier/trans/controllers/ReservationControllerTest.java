package multitier.trans.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import multitier.trans.dto.CreateReservationRequest;
import multitier.trans.model.Reservation;
import multitier.trans.model.Route;
import multitier.trans.model.RouteStop;
import multitier.trans.model.Station;
import multitier.trans.model.TripTimeDetails;
import multitier.trans.model.enums.PassengerCategory;
import multitier.trans.model.enums.ReservationStatus;
import multitier.trans.model.enums.StationStatus;
import multitier.trans.model.enums.VehicleClass;

import java.util.ArrayList;
import java.util.List;
import multitier.trans.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
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

        testRoute = new Route();
        testRoute.setId(1L);
        testRoute.setVehicleClass(VehicleClass.STANDARD);
        testRoute.setVehicleCapacity(VehicleClass.STANDARD.getSeatCapacity());
        testRoute.setDistance(100.0);
        testRoute.setDurationMinutes(90);

        List<RouteStop> stops = new ArrayList<>();

        RouteStop stop1 = new RouteStop();
        stop1.setRoute(testRoute);
        stop1.setStation(testStationA);
        stop1.setSequenceOrder(0);
        stop1.setDistanceFromPrevious(0.0);
        stop1.setDurationMinutesFromPrevious(0);
        stop1.setCumulativeDistance(0.0);
        stop1.setCumulativeDurationMinutes(0);
        stops.add(stop1);

        RouteStop stop2 = new RouteStop();
        stop2.setRoute(testRoute);
        stop2.setStation(testStationB);
        stop2.setSequenceOrder(1);
        stop2.setDistanceFromPrevious(100.0);
        stop2.setDurationMinutesFromPrevious(90);
        stop2.setCumulativeDistance(100.0);
        stop2.setCumulativeDurationMinutes(90);
        stops.add(stop2);

        testRoute.setRouteStops(stops);

        testDeparture = LocalDateTime.of(2025, 11, 20, 10, 0);
        testArrival = LocalDateTime.of(2025, 11, 20, 12, 0);
    }

    @Test
    @WithMockUser(roles = "USER")
    public void whenCreateReservation_withValidData_thenReturns201Created() throws Exception {
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

        when(reservationService.createReservation(any(CreateReservationRequest.class))).thenReturn(savedReservation);

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.passengerName").value("Test Passenger"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void whenCancelReservation_withValidId_thenReturns200OK() throws Exception {
        Reservation cancelledReservation = new Reservation();
        cancelledReservation.setId(1L);
        cancelledReservation.setRoute(testRoute);
        cancelledReservation.setPassengerName("Test Passenger");
        cancelledReservation.setStatus(ReservationStatus.CANCELLED);
        cancelledReservation.setPassengerCategory(PassengerCategory.ADULT);
        cancelledReservation.setVehicleClass(VehicleClass.STANDARD);

        when(reservationService.cancelReservation(1L)).thenReturn(cancelledReservation);

        mockMvc.perform(put("/api/reservations/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void whenCreateReservation_withInvalidData_thenReturns400BadRequest() throws Exception {
        CreateReservationRequest request = new CreateReservationRequest();
        request.setRouteId(1L);
        request.setPassengerName("");
        request.setSeatCount(2);
        request.setDepartureTime(testDeparture);
        request.setArrivalTime(testArrival);
        request.setPassengerCategory(PassengerCategory.ADULT);
        request.setVehicleClass(VehicleClass.STANDARD);

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenCreateReservation_withoutAuth_thenReturns403Forbidden() throws Exception {
        CreateReservationRequest request = new CreateReservationRequest();
        request.setRouteId(1L);
        request.setPassengerName("Test Passenger");
        request.setSeatCount(2);
        request.setDepartureTime(testDeparture);
        request.setArrivalTime(testArrival);
        request.setPassengerCategory(PassengerCategory.ADULT);
        request.setVehicleClass(VehicleClass.STANDARD);

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
