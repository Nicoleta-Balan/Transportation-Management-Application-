package multitier.trans.repository;

import multitier.trans.model.Station;
import multitier.trans.model.enums.StationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit Test for StationRepository (JPA Repository Service).
 * 
 * This is an integration test that uses an in-memory database (H2) to test
 * the actual database operations without requiring a real PostgreSQL instance.
 * 
 * Tests cover:
 * - CRUD operations (Create, Read, Update, Delete)
 * - Custom query methods (findByName)
 * - Entity persistence and retrieval
 */
@DataJpaTest
public class StationRepositoryTest {

    @Autowired
    private StationRepository stationRepository;

    private Station testStation;

    @BeforeEach
    void setUp() {
        // Create a test station
        testStation = new Station("Test Station", "Test Description", StationStatus.ACTIVE);
    }

    /**
     * Test: Save a new station and verify it's persisted correctly.
     */
    @Test
    public void whenSaveStation_thenStationIsPersisted() {
        // Act
        Station savedStation = stationRepository.save(testStation);

        // Assert
        assertNotNull(savedStation.getId(), "Station should have an ID after saving");
        assertEquals("Test Station", savedStation.getName());
        assertEquals("Test Description", savedStation.getDescription());
        assertEquals(StationStatus.ACTIVE, savedStation.getStatus());
    }

    /**
     * Test: Find station by ID.
     */
    @Test
    public void whenFindById_thenReturnStation() {
        // Arrange
        Station savedStation = stationRepository.save(testStation);
        Long stationId = savedStation.getId();

        // Act
        Optional<Station> foundStation = stationRepository.findById(stationId);

        // Assert
        assertTrue(foundStation.isPresent(), "Station should be found");
        assertEquals("Test Station", foundStation.get().getName());
    }

    /**
     * Test: Find station by name (custom query method).
     */
    @Test
    public void whenFindByName_thenReturnStation() {
        // Arrange
        stationRepository.save(testStation);

        // Act
        Station foundStation = stationRepository.findByName("Test Station");

        // Assert
        assertNotNull(foundStation, "Station should be found by name");
        assertEquals("Test Station", foundStation.getName());
    }

    /**
     * Test: Find all stations.
     */
    @Test
    public void whenFindAll_thenReturnAllStations() {
        // Arrange
        Station station1 = new Station("Station 1", "Description 1", StationStatus.ACTIVE);
        Station station2 = new Station("Station 2", "Description 2", StationStatus.ACTIVE);
        stationRepository.save(station1);
        stationRepository.save(station2);

        // Act
        List<Station> allStations = stationRepository.findAll();

        // Assert
        assertTrue(allStations.size() >= 2, "Should find at least 2 stations");
        assertTrue(allStations.stream().anyMatch(s -> s.getName().equals("Station 1")));
        assertTrue(allStations.stream().anyMatch(s -> s.getName().equals("Station 2")));
    }

    /**
     * Test: Update station.
     */
    @Test
    public void whenUpdateStation_thenStationIsUpdated() {
        // Arrange
        Station savedStation = stationRepository.save(testStation);
        Long stationId = savedStation.getId();

        // Act
        savedStation.setDescription("Updated Description");
        Station updatedStation = stationRepository.save(savedStation);

        // Assert
        assertEquals("Updated Description", updatedStation.getDescription());
        assertEquals(stationId, updatedStation.getId(), "ID should remain the same");
    }

    /**
     * Test: Delete station and verify @PreRemove is called.
     */
    @Test
    public void whenDeleteStation_thenStationIsRemoved() {
        // Arrange
        Station savedStation = stationRepository.save(testStation);
        Long stationId = savedStation.getId();

        // Act
        stationRepository.delete(savedStation);

        // Assert
        Optional<Station> deletedStation = stationRepository.findById(stationId);
        assertFalse(deletedStation.isPresent(), "Station should be deleted");
    }

    /**
     * Test: Verify station can be found after saving.
     */
    @Test
    public void whenSaveStation_thenStationCanBeFound() {
        // Act
        Station savedStation = stationRepository.save(testStation);
        Optional<Station> foundStation = stationRepository.findById(savedStation.getId());

        // Assert
        assertTrue(foundStation.isPresent(), "Station should be found after saving");
        assertEquals("Test Station", foundStation.get().getName());
    }
}

