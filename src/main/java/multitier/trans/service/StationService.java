package multitier.trans.service;

import multitier.trans.model.Station;
import java.util.List;

/**
 * Service Layer Interface for Station operations (implements SCRUM-17).
 * Defines the business logic contract for managing stations.
 */
public interface StationService {

    /**
     * Business logic for creating a new station.
     * @param station The station object to be saved.
     * @return The saved Station entity (with its new ID).
     */
    Station createStation(Station station);

    /**
     * Fetches all stations.
     * @return A list of all Station objects.
     */
    List<Station> getAllStations();
}