package multitier.trans.service;

import multitier.trans.model.Station;
import multitier.trans.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of the StationService.
 * This is the "brain" that contains the actual business logic for stations.
 */
@Service
public class StationServiceImpl implements StationService {

    private final StationRepository stationRepository;

    @Autowired
    public StationServiceImpl(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    /**
     * Saves the station. The validation (@Size, @NotNull)
     * will be triggered automatically by @Valid in the controller.
     */
    @Override
    public Station createStation(Station station) {
        // You can add more complex logic here, e.g.,
        // if (stationRepository.findByName(station.getName()) != null) {
        //     throw new RuntimeException("Station with this name already exists!");
        // }
        return stationRepository.save(station);
    }

    @Override
    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }
}