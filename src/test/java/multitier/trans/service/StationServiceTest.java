package multitier.trans.service;

import multitier.trans.model.Station;
import multitier.trans.model.enums.StationStatus;
import multitier.trans.repository.StationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * JUnit Test for StationService (Business Service).
 * 
 * Unit test using mocks to test business logic without database dependencies.
 * Tests business service methods and their interaction with repositories.
 */
@ExtendWith(MockitoExtension.class)
public class StationServiceTest {

    @Mock
    private StationRepository stationRepository;

    @InjectMocks
    private StationServiceImpl stationService;

    private Station testStation;

    @BeforeEach
    void setUp() {
        testStation = new Station("Test Station", "Test Description", StationStatus.ACTIVE);
        testStation.setId(1L);
    }

    /**
     * Test: Create station through service.
     */
    @Test
    public void whenCreateStation_thenRepositorySaveIsCalled() {
        // Arrange
        Station newStation = new Station("New Station", "New Description", StationStatus.ACTIVE);
        when(stationRepository.save(any(Station.class))).thenReturn(testStation);

        // Act
        Station result = stationService.createStation(newStation);

        // Assert
        assertNotNull(result);
        verify(stationRepository, times(1)).save(newStation);
    }

    /**
     * Test: Get all stations through service.
     */
    @Test
    public void whenGetAllStations_thenRepositoryFindAllIsCalled() {
        // Arrange
        Station station1 = new Station("Station 1", "Description 1", StationStatus.ACTIVE);
        Station station2 = new Station("Station 2", "Description 2", StationStatus.ACTIVE);
        List<Station> stations = Arrays.asList(station1, station2);
        
        when(stationRepository.findAll()).thenReturn(stations);

        // Act
        List<Station> result = stationService.getAllStations();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(stationRepository, times(1)).findAll();
    }

    /**
     * Test: Verify service returns the station from repository.
     */
    @Test
    public void whenCreateStation_thenServiceReturnsSavedStation() {
        // Arrange
        Station newStation = new Station("New Station", "New Description", StationStatus.ACTIVE);
        when(stationRepository.save(any(Station.class))).thenReturn(testStation);

        // Act
        Station result = stationService.createStation(newStation);

        // Assert
        assertEquals(testStation.getId(), result.getId());
        assertEquals(testStation.getName(), result.getName());
    }

    /**
     * Test: Verify service passes station to repository correctly.
     */
    @Test
    public void whenCreateStation_thenCorrectStationIsPassedToRepository() {
        // Arrange
        Station newStation = new Station("New Station", "New Description", StationStatus.ACTIVE);
        when(stationRepository.save(any(Station.class))).thenAnswer(invocation -> {
            Station station = invocation.getArgument(0);
            station.setId(1L);
            return station;
        });

        // Act
        Station result = stationService.createStation(newStation);

        // Assert
        verify(stationRepository).save(newStation);
        assertEquals("New Station", result.getName());
    }
}

