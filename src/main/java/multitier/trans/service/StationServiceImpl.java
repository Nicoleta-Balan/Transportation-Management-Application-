package multitier.trans.service;

import multitier.trans.model.Reservation;
import multitier.trans.model.Route;
import multitier.trans.model.Station;
import multitier.trans.repository.ReservationRepository;
import multitier.trans.repository.RouteRepository;
import multitier.trans.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of the StationService.
 * This is the "brain" that contains the actual business logic for stations.
 * Uses Spring Data JPA repositories for all data access operations.
 * Replaces database triggers for validation and denormalization.
 */
@Service
@Transactional
public class StationServiceImpl implements StationService {

    private final StationRepository stationRepository;
    private final RouteRepository routeRepository;
    private final ReservationRepository reservationRepository;

    @Autowired
    public StationServiceImpl(StationRepository stationRepository,
                             RouteRepository routeRepository,
                             ReservationRepository reservationRepository) {
        this.stationRepository = stationRepository;
        this.routeRepository = routeRepository;
        this.reservationRepository = reservationRepository;
    }

    /**
     * Saves the station. The validation (@Size, @NotNull)
     * will be triggered automatically by @Valid in the controller.
     */

    @Override
    public Station createStation(Station station) {
        return stationRepository.save(station);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    @Override
    public Station updateStation(Station station) {
        Station existingStation = stationRepository.findById(station.getId())
                .orElseThrow(() -> new RuntimeException("Station not found with id: " + station.getId()));

        // Validate status change: prevent setting station to CLOSED if it has routes
        // Note: The original trigger checked for routes with status='ACTIVE', but Route entity doesn't have a status field
        // So we check if the station is used in any routes (simpler validation)
        if (multitier.trans.model.enums.StationStatus.CLOSED.equals(station.getStatus()) && 
            !multitier.trans.model.enums.StationStatus.CLOSED.equals(existingStation.getStatus())) {
            List<Route> routes = routeRepository.findAll().stream()
                    .filter(route -> route.getOriginStation().getId().equals(station.getId()) || 
                                    route.getDestinationStation().getId().equals(station.getId()))
                    .toList();

            if (!routes.isEmpty()) {
                throw new RuntimeException("Cannot close station " + station.getName() + 
                        " because it has routes");
            }
        }

        // Update denormalized fields in reservations if station name changed
        String oldName = existingStation.getName();
        String newName = station.getName();
        if (oldName != null && !oldName.equals(newName)) {
            // Find all routes that use this station
            List<Route> routes = routeRepository.findAll().stream()
                    .filter(route -> route.getOriginStation().getId().equals(station.getId()) || 
                                    route.getDestinationStation().getId().equals(station.getId()))
                    .toList();

            // Update denormalized station names in reservations
            for (Route route : routes) {
                List<Reservation> reservations = reservationRepository.findByRouteId(route.getId());
                for (Reservation reservation : reservations) {
                    if (route.getOriginStation().getId().equals(station.getId())) {
                        reservation.setOriginStationName(newName);
                    }
                    if (route.getDestinationStation().getId().equals(station.getId())) {
                        reservation.setDestinationStationName(newName);
                    }
                    reservationRepository.save(reservation);
                }
            }
        }

        return stationRepository.save(station);
    }

    @Override
    public void deleteStation(Long stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station not found with id: " + stationId));

        // Check if station is used in routes
        List<Route> routes = routeRepository.findAll().stream()
                .filter(route -> route.getOriginStation().getId().equals(stationId) || 
                                route.getDestinationStation().getId().equals(stationId))
                .toList();

        if (!routes.isEmpty()) {
            throw new RuntimeException("Cannot delete station " + station.getName() + 
                    " because it is used in routes");
        }

        stationRepository.delete(station);
    }
}