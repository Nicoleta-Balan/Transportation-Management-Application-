package multitier.trans.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import multitier.trans.dto.CreateRouteRequest;
import multitier.trans.dto.RouteStopRequest;
import multitier.trans.dto.UpdateRouteRequest;
import multitier.trans.exception.ResourceNotFoundException;
import multitier.trans.model.Route;
import multitier.trans.model.RouteStop;
import multitier.trans.model.Station;
import multitier.trans.model.enums.StationStatus;
import multitier.trans.model.enums.VehicleClass;
import multitier.trans.service.RouteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RouteService routeService;

    @MockBean
    private RepositoryEntityLinks repositoryEntityLinks;

    private Route testRoute;
    private Station testStationA;
    private Station testStationB;
    private Station testStationC;

    @BeforeEach
    void setUp() {
        // Create test stations
        testStationA = new Station("Origin Station", "Description A", "Address A", 
                                   47.1788, 27.56716, StationStatus.ACTIVE);
        testStationA.setId(1L);

        testStationB = new Station("Intermediary Station", "Description B", "Address B", 
                                   47.1888, 27.57716, StationStatus.ACTIVE);
        testStationB.setId(2L);

        testStationC = new Station("Destination Station", "Description C", "Address C", 
                                   47.1988, 27.58716, StationStatus.ACTIVE);
        testStationC.setId(3L);

        // Create test route with route stops
        testRoute = new Route();
        testRoute.setId(1L);
        testRoute.setVehicleClass(VehicleClass.STANDARD);
        testRoute.setVehicleCapacity(VehicleClass.STANDARD.getSeatCapacity());
        testRoute.setDistance(100.0);
        testRoute.setDurationMinutes(90);
        testRoute.setDescription("Test Route");

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
        stop2.setDistanceFromPrevious(50.0);
        stop2.setDurationMinutesFromPrevious(45);
        stop2.setCumulativeDistance(50.0);
        stop2.setCumulativeDurationMinutes(45);
        stops.add(stop2);

        RouteStop stop3 = new RouteStop();
        stop3.setRoute(testRoute);
        stop3.setStation(testStationC);
        stop3.setSequenceOrder(2);
        stop3.setDistanceFromPrevious(50.0);
        stop3.setDurationMinutesFromPrevious(45);
        stop3.setCumulativeDistance(100.0);
        stop3.setCumulativeDurationMinutes(90);
        stops.add(stop3);

        testRoute.setRouteStops(stops);
    }

    @Test
    public void whenGetAllRoutes_thenReturns200Ok() throws Exception {
        List<Route> routes = Arrays.asList(testRoute);
        when(routeService.findAllRoutes()).thenReturn(routes);

        mockMvc.perform(get("/api/routes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].vehicleClass").value("STANDARD"))
                .andExpect(jsonPath("$[0].description").value("Test Route"));
    }

    @Test
    public void whenGetRouteById_withValidId_thenReturns200Ok() throws Exception {
        when(routeService.findRouteById(1L)).thenReturn(Optional.of(testRoute));

        mockMvc.perform(get("/api/routes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.vehicleClass").value("STANDARD"))
                .andExpect(jsonPath("$.description").value("Test Route"))
                .andExpect(jsonPath("$.routeStops").isArray())
                .andExpect(jsonPath("$.routeStops.length()").value(3));
    }

    @Test
    public void whenGetRouteById_withInvalidId_thenReturns404NotFound() throws Exception {
        when(routeService.findRouteById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/routes/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void whenCreateRoute_withValidData_thenReturns201Created() throws Exception {
        CreateRouteRequest request = new CreateRouteRequest();
        request.setVehicleClass(VehicleClass.STANDARD);
        request.setDistance(100.0);
        request.setDurationMinutes(90);
        request.setDescription("New Route");

        List<RouteStopRequest> stops = new ArrayList<>();
        RouteStopRequest stop1 = new RouteStopRequest();
        stop1.setStationId(1L);
        stop1.setSequenceOrder(0);
        stop1.setDistanceFromPrevious(0.0);
        stop1.setDurationMinutesFromPrevious(0);
        stops.add(stop1);

        RouteStopRequest stop2 = new RouteStopRequest();
        stop2.setStationId(2L);
        stop2.setSequenceOrder(1);
        stop2.setDistanceFromPrevious(100.0);
        stop2.setDurationMinutesFromPrevious(90);
        stops.add(stop2);

        request.setStops(stops);

        Route savedRoute = new Route();
        savedRoute.setId(2L);
        savedRoute.setVehicleClass(VehicleClass.STANDARD);
        savedRoute.setVehicleCapacity(VehicleClass.STANDARD.getSeatCapacity());
        savedRoute.setDistance(100.0);
        savedRoute.setDurationMinutes(90);
        savedRoute.setDescription("New Route");

        when(routeService.createRoute(any(CreateRouteRequest.class))).thenReturn(savedRoute);

        mockMvc.perform(post("/api/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.description").value("New Route"));
    }

    @Test
    public void whenCreateRoute_withInvalidData_thenReturns400BadRequest() throws Exception {
        CreateRouteRequest request = new CreateRouteRequest();
        request.setVehicleClass(null); // Invalid: null vehicle class
        request.setDistance(100.0);
        request.setDurationMinutes(90);
        request.setDescription("New Route");

        List<RouteStopRequest> stops = new ArrayList<>();
        RouteStopRequest stop1 = new RouteStopRequest();
        stop1.setStationId(1L);
        stop1.setSequenceOrder(0);
        stop1.setDistanceFromPrevious(0.0);
        stop1.setDurationMinutesFromPrevious(0);
        stops.add(stop1);

        RouteStopRequest stop2 = new RouteStopRequest();
        stop2.setStationId(2L);
        stop2.setSequenceOrder(1);
        stop2.setDistanceFromPrevious(100.0);
        stop2.setDurationMinutesFromPrevious(90);
        stops.add(stop2);

        request.setStops(stops);

        mockMvc.perform(post("/api/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenCreateRoute_withTooFewStops_thenReturns400BadRequest() throws Exception {
        CreateRouteRequest request = new CreateRouteRequest();
        request.setVehicleClass(VehicleClass.STANDARD);
        request.setDistance(100.0);
        request.setDurationMinutes(90);
        request.setDescription("New Route");

        List<RouteStopRequest> stops = new ArrayList<>();
        RouteStopRequest stop1 = new RouteStopRequest();
        stop1.setStationId(1L);
        stop1.setSequenceOrder(0);
        stop1.setDistanceFromPrevious(0.0);
        stop1.setDurationMinutesFromPrevious(0);
        stops.add(stop1);

        // Only 1 stop - invalid (needs at least 2)
        request.setStops(stops);

        mockMvc.perform(post("/api/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenCreateRoute_withInvalidDistance_thenReturns400BadRequest() throws Exception {
        CreateRouteRequest request = new CreateRouteRequest();
        request.setVehicleClass(VehicleClass.STANDARD);
        request.setDistance(0.5); // Invalid: less than 1 km
        request.setDurationMinutes(90);
        request.setDescription("New Route");

        List<RouteStopRequest> stops = new ArrayList<>();
        RouteStopRequest stop1 = new RouteStopRequest();
        stop1.setStationId(1L);
        stop1.setSequenceOrder(0);
        stop1.setDistanceFromPrevious(0.0);
        stop1.setDurationMinutesFromPrevious(0);
        stops.add(stop1);

        RouteStopRequest stop2 = new RouteStopRequest();
        stop2.setStationId(2L);
        stop2.setSequenceOrder(1);
        stop2.setDistanceFromPrevious(100.0);
        stop2.setDurationMinutesFromPrevious(90);
        stops.add(stop2);

        request.setStops(stops);

        mockMvc.perform(post("/api/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenUpdateRoute_withValidData_thenReturns200Ok() throws Exception {
        UpdateRouteRequest request = new UpdateRouteRequest();
        request.setVehicleClass(VehicleClass.STANDARD);
        request.setDistance(150.0);
        request.setDurationMinutes(120);
        request.setDescription("Updated Route");

        List<RouteStopRequest> stops = new ArrayList<>();
        RouteStopRequest stop1 = new RouteStopRequest();
        stop1.setStationId(1L);
        stop1.setSequenceOrder(0);
        stop1.setDistanceFromPrevious(0.0);
        stop1.setDurationMinutesFromPrevious(0);
        stops.add(stop1);

        RouteStopRequest stop2 = new RouteStopRequest();
        stop2.setStationId(2L);
        stop2.setSequenceOrder(1);
        stop2.setDistanceFromPrevious(150.0);
        stop2.setDurationMinutesFromPrevious(120);
        stops.add(stop2);

        request.setStops(stops);

        Route updatedRoute = new Route();
        updatedRoute.setId(1L);
        updatedRoute.setVehicleClass(VehicleClass.STANDARD);
        updatedRoute.setVehicleCapacity(VehicleClass.STANDARD.getSeatCapacity());
        updatedRoute.setDistance(150.0);
        updatedRoute.setDurationMinutes(120);
        updatedRoute.setDescription("Updated Route");

        when(routeService.updateRoute(eq(1L), any(UpdateRouteRequest.class)))
                .thenReturn(updatedRoute);

        mockMvc.perform(put("/api/routes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Updated Route"))
                .andExpect(jsonPath("$.vehicleClass").value("STANDARD"));
    }

    @Test
    public void whenUpdateRoute_withInvalidId_thenReturns404NotFound() throws Exception {
        UpdateRouteRequest request = new UpdateRouteRequest();
        request.setVehicleClass(VehicleClass.STANDARD);
        request.setDistance(100.0);
        request.setDurationMinutes(90);
        request.setDescription("Updated Route");

        List<RouteStopRequest> stops = new ArrayList<>();
        RouteStopRequest stop1 = new RouteStopRequest();
        stop1.setStationId(1L);
        stop1.setSequenceOrder(0);
        stop1.setDistanceFromPrevious(0.0);
        stop1.setDurationMinutesFromPrevious(0);
        stops.add(stop1);

        RouteStopRequest stop2 = new RouteStopRequest();
        stop2.setStationId(2L);
        stop2.setSequenceOrder(1);
        stop2.setDistanceFromPrevious(100.0);
        stop2.setDurationMinutesFromPrevious(90);
        stops.add(stop2);

        request.setStops(stops);

        when(routeService.updateRoute(eq(999L), any(UpdateRouteRequest.class)))
                .thenThrow(new ResourceNotFoundException("Route", 999L));

        mockMvc.perform(put("/api/routes/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void whenDeleteRoute_withValidId_thenReturns204NoContent() throws Exception {
        doNothing().when(routeService).deleteRoute(1L);

        mockMvc.perform(delete("/api/routes/1"))
                .andExpect(status().isNoContent());

        verify(routeService, times(1)).deleteRoute(1L);
    }

    @Test
    public void whenDeleteRoute_withInvalidId_thenReturns404NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Route", 999L))
                .when(routeService).deleteRoute(999L);

        mockMvc.perform(delete("/api/routes/999"))
                .andExpect(status().isNotFound());

        verify(routeService, times(1)).deleteRoute(999L);
    }
}

