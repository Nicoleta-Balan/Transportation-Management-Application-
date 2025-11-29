package multitier.trans.service;

import multitier.trans.dto.CreateStationRequest;
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
     * Creates a new station from the DTO request.
     * The validation (@Size, @NotNull) will be triggered automatically by @Valid in the controller.
     */
    @Override
    public Station createStation(CreateStationRequest request) {
        Station newStation = new Station();
        newStation.setName(request.getName());
        newStation.setDescription(request.getDescription());
        newStation.setStatus(request.getStatus());
        return stationRepository.save(newStation);
    }

    @Override
    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }
}