package multitier.trans.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import multitier.trans.dto.CreateStationRequest;
import multitier.trans.dto.UpdateStationRequest;
import multitier.trans.exception.ResourceNotFoundException;
import multitier.trans.model.Station;
import multitier.trans.model.enums.StationStatus;
import multitier.trans.service.StationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(multitier.trans.controllers.rest.StationRestController.class)
public class StationControllerTest {

    @Autowired
    private MockMvc mockMvc; // A tool to fake HTTP requests

    @Autowired
    private ObjectMapper objectMapper; // A tool to convert Java objects to JSON

    @MockBean
    private StationService stationService; // A "fake" version of the service

    @Test
    public void whenCreateStation_withInvalidName_thenReturns400BadRequest() throws Exception {
        // 1. Arrange: Create a station request with an invalid name ("X" has 1 char, rule needs 2)
        CreateStationRequest invalidRequest = new CreateStationRequest();
        invalidRequest.setName("X");
        invalidRequest.setDescription("Valid Description");
        invalidRequest.setAddress("123 Test Street");
        invalidRequest.setLatitude(47.1788);
        invalidRequest.setLongitude(27.56716);
        invalidRequest.setStatus(StationStatus.ACTIVE);

        // 2. Act & 3. Assert
        mockMvc.perform(post("/api/stations") // Send a POST request
                        .contentType(MediaType.APPLICATION_JSON) // as JSON
                        .content(objectMapper.writeValueAsString(invalidRequest))) // with the invalid data
                .andExpect(status().isBadRequest()); // Assert that we get a 400 Bad Request
    }

    @Test
    public void whenCreateStation_withNullName_thenReturns400BadRequest() throws Exception {
        // 1. Arrange: Create a station request with a null name
        CreateStationRequest invalidRequest = new CreateStationRequest();
        invalidRequest.setName(null);
        invalidRequest.setDescription("Valid Description");
        invalidRequest.setAddress("123 Test Street");
        invalidRequest.setLatitude(47.1788);
        invalidRequest.setLongitude(27.56716);
        invalidRequest.setStatus(StationStatus.ACTIVE);

        // 2. Act & 3. Assert
        mockMvc.perform(post("/api/stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()); // Assert that we get a 400 Bad Request
    }

    @Test
    public void whenCreateStation_withValidData_thenReturns201Created() throws Exception {
        // 1. Arrange: Create a station request with valid data
        CreateStationRequest validRequest = new CreateStationRequest();
        validRequest.setName("Iasi");
        validRequest.setDescription("Main Bus Station");
        validRequest.setAddress("123 Main Street, Iasi");
        validRequest.setLatitude(47.1788);
        validRequest.setLongitude(27.56716);
        validRequest.setStatus(StationStatus.ACTIVE);

        // Mock the service response
        Station savedStation = new Station("Iasi", "Main Bus Station", "address", 47.1788, 27.56716, StationStatus.ACTIVE);
        savedStation.setId(1L);
        when(stationService.createStation(any(CreateStationRequest.class))).thenReturn(savedStation);

        // 2. Act & 3. Assert
        mockMvc.perform(post("/api/stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated()); // Assert that we get a 201 Created
    }

    @Test
    public void whenUpdateStation_withValidData_thenReturns200Ok() throws Exception {
        UpdateStationRequest updateRequest = new UpdateStationRequest();
        updateRequest.setDescription("Updated description");
        updateRequest.setAddress("456 Updated Street");
        updateRequest.setLatitude(48.1788);
        updateRequest.setLongitude(28.56716);
        updateRequest.setStatus(StationStatus.INACTIVE);

        // Mock valid response
        Station updatedStation = new Station("Iasi", "Updated description", "456 Updated Street", 
                                              48.1788, 28.56716, StationStatus.INACTIVE);
        updatedStation.setId(1L);
        when(stationService.updateStation(eq(1L), any(UpdateStationRequest.class)))
                .thenReturn(updatedStation);

        mockMvc.perform(put("/api/stations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk()); // 200 OK
    }

    @Test
    public void whenUpdateStation_withInvalidId_thenReturns404NotFound() throws Exception {
        UpdateStationRequest updateRequest = new UpdateStationRequest();
        updateRequest.setDescription("Updated description");
        updateRequest.setAddress("456 Updated Street");
        updateRequest.setLatitude(48.1788);
        updateRequest.setLongitude(28.56716);
        updateRequest.setStatus(StationStatus.INACTIVE);

        // Mock invalid ID - service now throws ResourceNotFoundException
        when(stationService.updateStation(eq(999L), any(UpdateStationRequest.class)))
                .thenThrow(new ResourceNotFoundException("Station", 999L));

        mockMvc.perform(put("/api/stations/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound()); // 404 Not Found
    }

    @Test
    public void whenDeleteStation_withValidId_thenReturns204NoContent() throws Exception {
        doNothing().when(stationService).deleteStation(1L);

        mockMvc.perform(delete("/api/stations/1"))
                .andExpect(status().isNoContent()); // 204 No Content
    }

    @Test
    public void whenDeleteStation_withInvalidId_thenReturns404NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Station", 999L))
                .when(stationService).deleteStation(999L);

        mockMvc.perform(delete("/api/stations/999"))
                .andExpect(status().isNotFound()); // 404 Not Found
    }
}