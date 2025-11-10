package multitier.trans.service;

import multitier.trans.dto.AddRouteTimetableEntriesRequest;
import multitier.trans.dto.CreateRouteTimetableRequest;
import multitier.trans.dto.RouteTimetableEntryRequest;
import multitier.trans.dto.RouteTimetableEntryResponse;
import multitier.trans.dto.RouteTimetableResponse;
import multitier.trans.model.Route;
import multitier.trans.model.RouteTimetable;
import multitier.trans.model.RouteTimetableEntry;
import multitier.trans.repository.RouteRepository;
import multitier.trans.repository.RouteTimetableRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TimetableServiceImpl implements TimetableService {

    private final RouteRepository routeRepository;
    private final RouteTimetableRepository routeTimetableRepository;

    public TimetableServiceImpl(RouteRepository routeRepository,
                                RouteTimetableRepository routeTimetableRepository) {
        this.routeRepository = routeRepository;
        this.routeTimetableRepository = routeTimetableRepository;
    }

    @Override
    public RouteTimetableResponse createTimetable(Long routeId, CreateRouteTimetableRequest request) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Route not found with id " + routeId));

        RouteTimetable timetable = new RouteTimetable();
        timetable.setRoute(route);
        timetable.setName(request.getName());
        timetable.setDescription(request.getDescription());
        timetable.setEffectiveFrom(request.getEffectiveFrom());
        timetable.setEffectiveTo(request.getEffectiveTo());
        timetable.setStatus(request.getStatus() == null ? "ACTIVE" : request.getStatus());

        if (request.getEntries() != null) {
            for (RouteTimetableEntryRequest entryRequest : request.getEntries()) {
                timetable.addEntry(buildEntryFromRequest(entryRequest));
            }
        }

        RouteTimetable saved = routeTimetableRepository.save(timetable);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteTimetableResponse> getTimetablesForRoute(Long routeId) {
        if (!routeRepository.existsById(routeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Route not found with id " + routeId);
        }

        return routeTimetableRepository.findByRouteId(routeId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RouteTimetableResponse addEntries(Long routeId, Long timetableId, AddRouteTimetableEntriesRequest request) {
        RouteTimetable timetable = routeTimetableRepository.findByIdAndRouteId(timetableId, routeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Timetable " + timetableId + " not found for route " + routeId));

        for (RouteTimetableEntryRequest entryRequest : request.getEntries()) {
            timetable.addEntry(buildEntryFromRequest(entryRequest));
        }

        RouteTimetable saved = routeTimetableRepository.save(timetable);
        return mapToResponse(saved);
    }

    private RouteTimetableEntry buildEntryFromRequest(RouteTimetableEntryRequest request) {
        if (request.getServiceDay() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Service day is required");
        }

        DayOfWeek dayOfWeek = request.getServiceDay();
        RouteTimetableEntry entry = new RouteTimetableEntry();
        entry.setServiceDay(dayOfWeek);

        if (request.getDepartureTime() == null || request.getArrivalTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Departure and arrival times are required");
        }
        if (!request.getArrivalTime().isAfter(request.getDepartureTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Arrival time must be after departure time");
        }
        entry.setDepartureTime(request.getDepartureTime());
        entry.setArrivalTime(request.getArrivalTime());
        entry.setNotes(request.getNotes());
        return entry;
    }

    private RouteTimetableResponse mapToResponse(RouteTimetable timetable) {
        RouteTimetableResponse response = new RouteTimetableResponse();
        response.setId(timetable.getId());
        response.setRouteId(timetable.getRoute() != null ? timetable.getRoute().getId() : null);
        response.setName(timetable.getName());
        response.setDescription(timetable.getDescription());
        response.setEffectiveFrom(timetable.getEffectiveFrom());
        response.setEffectiveTo(timetable.getEffectiveTo());
        response.setStatus(timetable.getStatus());

        List<RouteTimetableEntryResponse> entryResponses = timetable.getEntries() == null
                ? new ArrayList<>()
                : timetable.getEntries().stream()
                .map(entry -> {
                    RouteTimetableEntryResponse entryResponse = new RouteTimetableEntryResponse();
                    entryResponse.setId(entry.getId());
                    entryResponse.setServiceDay(entry.getServiceDay());
                    entryResponse.setDepartureTime(entry.getDepartureTime());
                    entryResponse.setArrivalTime(entry.getArrivalTime());
                    entryResponse.setNotes(entry.getNotes());
                    return entryResponse;
                })
                .collect(Collectors.toList());

        response.setEntries(entryResponses);
        return response;
    }
}

