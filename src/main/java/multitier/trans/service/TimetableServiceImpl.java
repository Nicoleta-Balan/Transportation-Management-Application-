package multitier.trans.service;

import multitier.trans.dto.CreateTimetableRequest;
import multitier.trans.dto.UpdateTimetableRequest;
import multitier.trans.dto.TimetableStopRequest;
import multitier.trans.factory.TimetableFactory;
import multitier.trans.model.Timetable;
import multitier.trans.model.TimetableStop;
import multitier.trans.repository.TimetableRepository;
import multitier.trans.repository.StationRepository;
import multitier.trans.utils.RepositoryUtils;
import multitier.trans.utils.ValidationUtils;
import multitier.trans.utils.StopCreationHelper;
import multitier.trans.utils.TimetableStopCreationStrategy;
import multitier.trans.utils.JpaUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

@Service
public class TimetableServiceImpl implements TimetableService {

    private final TimetableRepository timetableRepository;
    private final EntityManager entityManager;
    private final TimetableFactory timetableFactory;
    private final StopCreationHelper<TimetableStop, TimetableStopRequest, Timetable> stopCreationHelper;

    // Custom constructor needed for stopCreationHelper initialization
    public TimetableServiceImpl(
            TimetableRepository timetableRepository,
            StationRepository stationRepository,
            EntityManager entityManager,
            TimetableFactory timetableFactory) {
        this.timetableRepository = timetableRepository;
        this.entityManager = entityManager;
        this.timetableFactory = timetableFactory;
        this.stopCreationHelper = new StopCreationHelper<>(
                new TimetableStopCreationStrategy(),
                stationRepository,
                TimetableStopRequest::getStationId,
                TimetableStopRequest::getSequenceOrder
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Timetable> findAllTimetablesByRouteId(Long routeId) {
        List<Timetable> timetables = timetableRepository.findByRouteIdWithStops(routeId);
        // Route.routeStops is ignored in JSON serialization via @JsonIgnoreProperties
        // No need to initialize it, and we can't fetch it in the same query anyway
        // (would cause MultipleBagFetchException)
        return timetables;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Timetable> findTimetableById(Long id) {
        return timetableRepository.findByIdWithStops(id);
    }

    @Override
    @Transactional
    public Timetable createTimetable(CreateTimetableRequest request) {
        // Create timetable using factory
        Timetable timetable = timetableFactory.createTimetable(request);

        // Save timetable (stops will be saved via cascade)
        Timetable savedTimetable = timetableRepository.save(timetable);
        
        // Flush to ensure stops are persisted immediately
        JpaUtils.flush(entityManager);

        // Reload with stops to ensure they're included in response
        Timetable reloaded = reloadTimetableWithStops(savedTimetable.getId());
        return reloaded;
    }

    @Override
    @Transactional
    public Timetable updateTimetable(Long id, UpdateTimetableRequest request) {
        // Load timetable with stops
        Timetable existingTimetable = RepositoryUtils.findByIdOrThrow(
            timetableRepository.findByIdWithStops(id),
            "Timetable",
            id
        );

        // Validate stops if provided
        if (request.getStops() != null) {
            ValidationUtils.validateSequenceOrder(
                request.getStops(),
                1,
                "Timetable",
                TimetableStopRequest::getSequenceOrder
            );
        }

        // Update description
        if (request.getDescription() != null) {
            existingTimetable.setDescription(request.getDescription());
        }

        // Update dates
        if (request.getStartDate() != null) {
            existingTimetable.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            existingTimetable.setEndDate(request.getEndDate());
        }
        if (request.getDaysOfWeek() != null) {
            existingTimetable.getDaysOfWeek().clear();
            existingTimetable.getDaysOfWeek().addAll(request.getDaysOfWeek());
        }

        // Update stops if provided
        if (request.getStops() != null) {
            JpaUtils.clearCollectionAndFlush(existingTimetable.getTimetableStops(), entityManager);
            
            List<TimetableStop> newStops = createTimetableStops(existingTimetable, request.getStops());
            // Directly add stops to collection and ensure bidirectional relationship
            for (TimetableStop stop : newStops) {
                // Ensure bidirectional relationship is set
                if (stop.getTimetable() != existingTimetable) {
                    stop.setTimetable(existingTimetable);
                }
                existingTimetable.getTimetableStops().add(stop);
            }
        }

        // Save timetable
        Timetable updatedTimetable = timetableRepository.save(existingTimetable);
        
        // Force initialization of collections
        updatedTimetable.getTimetableStops().size();
        updatedTimetable.getDaysOfWeek().size();

        return reloadTimetableWithStops(updatedTimetable.getId());
    }

    @Override
    @Transactional
    public void deleteTimetable(Long id) {
        RepositoryUtils.deleteByIdOrThrow(timetableRepository, "Timetable", id);
    }

    private Timetable reloadTimetableWithStops(Long timetableId) {
        return RepositoryUtils.reloadWithStops(
            timetableRepository::findByIdWithStops,
            "Timetable",
            timetableId
        );
    }

    private List<TimetableStop> createTimetableStops(Timetable timetable, List<TimetableStopRequest> stopRequests) {
        return stopCreationHelper.createStops(timetable, stopRequests);
    }

}

