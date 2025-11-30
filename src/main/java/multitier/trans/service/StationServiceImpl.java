package multitier.trans.service;

import multitier.trans.dto.CreateStationRequest;
import multitier.trans.model.Station;
import multitier.trans.repository.StationRepository;
import multitier.trans.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import multitier.trans.dto.UpdateStationRequest;

import java.util.List;

@Service
public class StationServiceImpl implements StationService {

    private final StationRepository stationRepository;
    private final RouteRepository routeRepository;

    @Autowired
    public StationServiceImpl(StationRepository stationRepository, RouteRepository routeRepository) {
        this.stationRepository = stationRepository;
        this.routeRepository = routeRepository;
    }

    @Override
    public Station createStation(CreateStationRequest request) {
        // Validate name uniqueness before creating
        Station existingStationWithName = stationRepository.findByName(request.getName());
        if (existingStationWithName != null) {
            throw new IllegalArgumentException("Station with name '" + request.getName() + "' already exists");
        }

        // Validate address uniqueness before creating
        Station existingStationWithAddress = stationRepository.findByAddress(request.getAddress());
        if (existingStationWithAddress != null) {
            throw new IllegalArgumentException("Station with address '" + request.getAddress() + "' already exists");
        }

        // All validations passed, create the new station
        Station newStation = new Station();
        newStation.setName(request.getName());
        newStation.setDescription(request.getDescription());
        newStation.setAddress(request.getAddress());
        newStation.setLatitude(request.getLatitude());
        newStation.setLongitude(request.getLongitude());
        newStation.setStatus(request.getStatus());
        return stationRepository.save(newStation);
    }

    @Override
    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    @Override
    public Station updateStation(Long id, UpdateStationRequest request) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Station with id " + id + " not found"));

        boolean addressChanged = !station.getAddress().equals(request.getAddress());

        if (addressChanged) {
            Station existingStationWithAddress = stationRepository.findByAddressAndIdNot(request.getAddress(), id);
            if (existingStationWithAddress != null) {
                throw new IllegalArgumentException("Station with address " + request.getAddress() + " already exists");
            }
        }

        station.setDescription(request.getDescription());
        station.setAddress(request.getAddress());
        station.setLatitude(request.getLatitude());
        station.setLongitude(request.getLongitude());
        station.setStatus(request.getStatus());

        return stationRepository.save(station);
    }

    @Override
    public void deleteStation(Long id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Station with id " + id + " not found")); 
        // Check if station is used as origin in any routes
        boolean isUsedAsOrigin = routeRepository.existsByOriginStationId(id);
        if (isUsedAsOrigin) {
            throw new IllegalArgumentException(
                "Cannot delete station '" + station.getName() + "' because it is used as origin station in one or more routes"
            );
        }
        // Check if station is used as destination in any routes
        boolean isUsedAsDestination = routeRepository.existsByDestinationStationId(id);
        if (isUsedAsDestination) {
            throw new IllegalArgumentException(
                "Cannot delete station '" + station.getName() + "' because it is used as destination station in one or more routes"
            );
        }
        // If no routes reference this station, safe to delete
        stationRepository.delete(station);
    }
}