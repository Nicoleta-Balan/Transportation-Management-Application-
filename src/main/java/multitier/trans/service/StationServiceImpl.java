package multitier.trans.service;

import multitier.trans.dto.CreateStationRequest;
import multitier.trans.model.Station;
import multitier.trans.model.Reservation;
import multitier.trans.model.enums.StationStatus;
import multitier.trans.repository.StationRepository;
import multitier.trans.repository.RouteStopRepository;
import multitier.trans.exception.ValidationException;
import multitier.trans.exception.BusinessException;
import multitier.trans.utils.RepositoryUtils;
import multitier.trans.utils.HybridDtoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import multitier.trans.dto.UpdateStationRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
@RequiredArgsConstructor  // Lombok: Generates constructor for final fields
public class StationServiceImpl implements StationService {

    private static final Logger logger = LoggerFactory.getLogger(StationServiceImpl.class);

    private final StationRepository stationRepository;
    private final RouteStopRepository routeStopRepository;
    private final ReservationService reservationService;

    @Override
    public Station createStation(CreateStationRequest request) {
        // Validate name and address uniqueness before creating
        validateNameUniqueness(request.getName(), null);
        validateAddressUniqueness(request.getAddress(), null);

        // Map DTO fields to entity using hybrid mapper
        // Note: name field is not in StationRequestBase, so we set it separately
        Station newStation = HybridDtoMapper.mapSimpleFieldsToNew(request, Station.class);
        newStation.setName(request.getName()); // Only field not in StationRequestBase
        return stationRepository.save(newStation);
    }

    @Override
    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    @Override
    public Station getStationById(Long id) {
        return RepositoryUtils.findByIdOrThrow(
            stationRepository.findById(id),
            "Station",
            id
        );
    }

    @Override
    @Transactional
    public Station updateStation(Long id, UpdateStationRequest request) {
        Station station = RepositoryUtils.findByIdOrThrow(
            stationRepository.findById(id),
            "Station",
            id
        );

        StationStatus oldStatus = station.getStatus();
        StationStatus newStatus = request.getStatus();

        // Check if status is changing to INACTIVE or MAINTENANCE
        if (newStatus != null && newStatus != oldStatus &&
            (newStatus == StationStatus.INACTIVE || newStatus == StationStatus.MAINTENANCE)) {

            // Find confirmed reservations using this station
            List<Reservation> confirmedReservations = 
                reservationService.findConfirmedReservationsForStation(station.getId());

            if (!confirmedReservations.isEmpty()) {
                throw new BusinessException(
                    String.format("Cannot change station '%s' to %s: %d confirmed reservations exist. " +
                        "Please cancel or reschedule these reservations first.",
                        station.getName(), newStatus, confirmedReservations.size())
                );
            }

            // Find pending reservations (allow with warning)
            List<Reservation> pendingReservations = 
                reservationService.findPendingReservationsForStation(station.getId());

            if (!pendingReservations.isEmpty()) {
                logger.warn("Station '{}' changed to {} with {} pending reservations. " +
                    "These reservations will be blocked from confirmation.",
                    station.getName(), newStatus, pendingReservations.size());
            }
        }

        // Validate address uniqueness if it changed
        boolean addressChanged = !station.getAddress().equals(request.getAddress());
        if (addressChanged) {
            validateAddressUniqueness(request.getAddress(), id);
        }

        // Map DTO fields to existing entity using hybrid mapper
        // Note: name cannot be updated, so it's not in UpdateStationRequest
        HybridDtoMapper.mapSimpleFields(request, station);
        
        return stationRepository.save(station);
    }

    @Override
    public void deleteStation(Long id) {
        Station station = RepositoryUtils.findByIdOrThrow(
            stationRepository.findById(id),
            "Station",
            id
        ); 
        
        // Check if station is used in any route stops
        boolean isUsedInRoutes = routeStopRepository.findAll().stream()
            .anyMatch(stop -> stop.getStation().getId().equals(id));
        
        if (isUsedInRoutes) {
            throw new BusinessException(
                "Cannot delete station '" + station.getName() + "' because it is used in one or more routes"
            );
        }
        
        // If no routes reference this station, safe to delete
        stationRepository.delete(station);
    }

    private void validateNameUniqueness(String name, Long excludeId) {
        Station existing = stationRepository.findByName(name);
        if (existing != null && (excludeId == null || !existing.getId().equals(excludeId))) {
            throw new ValidationException("Station with name '" + name + "' already exists");
        }
    }

    private void validateAddressUniqueness(String address, Long excludeId) {
        Station existing = excludeId != null
            ? stationRepository.findByAddressAndIdNot(address, excludeId)
            : stationRepository.findByAddress(address);
        if (existing != null) {
            throw new ValidationException("Station with address '" + address + "' already exists");
        }
    }
}