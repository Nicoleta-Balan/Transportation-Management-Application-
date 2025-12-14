package multitier.trans.service;

import multitier.trans.dto.CreateTimetableRequest;
import multitier.trans.dto.UpdateTimetableRequest;
import multitier.trans.model.Timetable;

import java.util.List;
import java.util.Optional;

public interface TimetableService {

    List<Timetable> findAllTimetablesByRouteId(Long routeId);

    Optional<Timetable> findTimetableById(Long id);

    Timetable createTimetable(CreateTimetableRequest request);

    Timetable updateTimetable(Long id, UpdateTimetableRequest request);

    void deleteTimetable(Long id);
}

