package multitier.trans.factory;

import multitier.trans.dto.CreateTimetableRequest;
import multitier.trans.dto.TimetableStopRequest;
import multitier.trans.model.Route;
import multitier.trans.model.Timetable;
import multitier.trans.model.TimetableStop;
import multitier.trans.repository.RouteRepository;
import multitier.trans.repository.StationRepository;
import multitier.trans.exception.ValidationException;
import multitier.trans.utils.RepositoryUtils;
import multitier.trans.utils.StopCreationHelper;
import multitier.trans.utils.TimetableStopCreationStrategy;
import multitier.trans.utils.ValidationUtils;
import multitier.trans.utils.StopCollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TimetableFactory {

    private final RouteRepository routeRepository;
    private final StopCreationHelper<TimetableStop, TimetableStopRequest, Timetable> stopCreationHelper;

    public TimetableFactory(RouteRepository routeRepository, StationRepository stationRepository) {
        this.routeRepository = routeRepository;
        this.stopCreationHelper = new StopCreationHelper<>(
                new TimetableStopCreationStrategy(),
                stationRepository,
                TimetableStopRequest::getStationId,
                TimetableStopRequest::getSequenceOrder
        );
    }

    public Timetable createTimetable(CreateTimetableRequest request) {
        // Validate route exists and load it with stops
        Route route = RepositoryUtils.findByIdOrThrow(
            routeRepository.findByIdWithStops(request.getRouteId()),
            "Route",
            request.getRouteId()
        );
        
        // Validate that route has stops
        if (route.getRouteStops() == null || route.getRouteStops().isEmpty()) {
            throw new ValidationException(
                "Route must have at least one stop to create a timetable"
            );
        }

        // Validate stops
        ValidationUtils.validateSequenceOrder(
            request.getStops(),
            1,
            "Timetable",
            TimetableStopRequest::getSequenceOrder
        );

        // Create timetable
        Timetable timetable = new Timetable();
        timetable.setRoute(route);
        timetable.setDescription(request.getDescription());
        timetable.setStartDate(request.getStartDate());
        timetable.setEndDate(request.getEndDate());
        
        // Initialize daysOfWeek collection (conditional logic)
        if (request.getDaysOfWeek() != null && !request.getDaysOfWeek().isEmpty()) {
            timetable.setDaysOfWeek(new ArrayList<>(request.getDaysOfWeek()));
        } else {
            timetable.setDaysOfWeek(new ArrayList<>());
        }

        // Create timetable stops
        List<TimetableStop> stops = stopCreationHelper.createStops(timetable, request.getStops());
        
        // Add stops to collection, ensuring bidirectional relationship is maintained
        // This ensures JPA change tracking works correctly
        StopCollectionUtils.addStopsToCollection(
            timetable.getTimetableStops(),
            stops,
            timetable,
            TimetableStop::setTimetable
        );

        return timetable;
    }
}

