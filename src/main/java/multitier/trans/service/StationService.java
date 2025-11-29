package multitier.trans.service;

import multitier.trans.dto.CreateStationRequest;
import multitier.trans.model.Station;
import java.util.List;

/**
 * Service Layer Interface for Station operations
 * Defines the business logic contract for managing stations.
 */
public interface StationService {

    /**
     * Business logic for creating a new station.
     * @param request The DTO containing station creation data.
     * @return The saved Station entity (with its new ID).
     */
    Station createStation(CreateStationRequest request);

    /**
     * Fetches all stations.
     * @return A list of all Station objects.
     */
    List<Station> getAllStations();
}