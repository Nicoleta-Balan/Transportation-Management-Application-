package multitier.trans.service;

import multitier.trans.model.Station;
import java.util.List;

/**
 * Service Layer Interface for Station operations
 */

public interface StationService {

    // Business logic for creating a new station.

    Station createStation(Station station);


//Fetches all stations

    List<Station> getAllStations();
}