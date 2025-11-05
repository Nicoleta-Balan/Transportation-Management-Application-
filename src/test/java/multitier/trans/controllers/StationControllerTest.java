package multitier.trans.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import multitier.trans.domain.model.Station;
import multitier.trans.application.service.StationService;
import multitier.trans.interfaces.StationController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * JUnit Test for Station Validation Service (implements SCRUM-19).
 *
 * This test checks if the validation rules (@Size, @NotNull)
 * on the Station entity are working correctly.
 */
@WebMvcTest(StationController.class) // We only want to test the Controller layer
public class StationControllerTest {

    @Autowired
    private MockMvc mockMvc; // A tool to fake HTTP requests

    @Autowired
    private ObjectMapper objectMapper; // A tool to convert Java objects to JSON

    @MockBean
    private StationService stationService; // A "fake" version of the service

    /**
     * Tests if the API correctly rejects a Station with an invalid name (too short).
     * This proves our @Size(min=2) validation rule is working.
     */

    @Test
    public void whenCreateStation_withInvalidName_thenReturns400BadRequest() throws Exception {
        // 1. Arrange: Create a station with an invalid name ("X" has 1 char, rule needs 2)
        Station invalidStation = new Station("X", "Valid Description", "Active");

        // 2. Act & 3. Assert
        mockMvc.perform(post("/api/stations") // Send a POST request
                        .contentType(MediaType.APPLICATION_JSON) // as JSON
                        .content(objectMapper.writeValueAsString(invalidStation))) // with the invalid data
                .andExpect(status().isBadRequest()); // Assert that we get a 400 Bad Request
    }

    /**
     * Test Case:
     * Tests if the API correctly rejects a Station with a null name.
     * This proves our @NotNull validation rule is working.
     */
    @Test
    public void whenCreateStation_withNullName_thenReturns400BadRequest() throws Exception {
        // 1. Arrange: Create a station with a null name
        Station invalidStation = new Station(null, "Valid Description", "Active");

        // 2. Act & 3. Assert
        mockMvc.perform(post("/api/stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidStation)))
                .andExpect(status().isBadRequest()); // Assert that we get a 400 Bad Request
    }

    /**
     * "Happy Path" Test Case:
     * Tests if the API accepts a station with valid data.
     */
    @Test
    public void whenCreateStation_withValidData_thenReturns201Created() throws Exception {
        // 1. Arrange: Create a station with valid data
        Station validStation = new Station("Iasi", "Main Bus Station", "Active");

        // 2. Act & 3. Assert
        mockMvc.perform(post("/api/stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validStation)))
                .andExpect(status().isCreated()); // Assert that we get a 201 Created
    }
}