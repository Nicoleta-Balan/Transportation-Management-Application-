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

    /**
     * Updates a station with validation.
     * Validates status changes and updates denormalized fields if station name changes.
     * 
     * @param station The station to update
     * @return The updated station
     * @throws RuntimeException if validation fails
     */
    Station updateStation(Station station);

    /**
     * Deletes a station with validation.
     * Prevents deletion if the station is used in routes.
     * 
     * @param stationId The ID of the station to delete
     * @throws RuntimeException if the station cannot be deleted
     */
    void deleteStation(Long stationId);
}