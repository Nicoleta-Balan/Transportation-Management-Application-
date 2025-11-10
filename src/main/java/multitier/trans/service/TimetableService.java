package multitier.trans.service;

import multitier.trans.dto.AddRouteTimetableEntriesRequest;
import multitier.trans.dto.CreateRouteTimetableRequest;
import multitier.trans.dto.RouteTimetableResponse;

import java.util.List;

public interface TimetableService {

    RouteTimetableResponse createTimetable(Long routeId, CreateRouteTimetableRequest request);

    List<RouteTimetableResponse> getTimetablesForRoute(Long routeId);

    RouteTimetableResponse addEntries(Long routeId, Long timetableId, AddRouteTimetableEntriesRequest request);
}

