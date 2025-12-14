package multitier.trans.service;

import multitier.trans.dto.CreateStationRequest;
import multitier.trans.dto.UpdateStationRequest;
import multitier.trans.model.Station;
import java.util.List;

public interface StationService {

    Station createStation(CreateStationRequest request);

    List<Station> getAllStations();

    Station getStationById(Long id);

    Station updateStation(Long id, UpdateStationRequest request);

    void deleteStation(Long id);
}