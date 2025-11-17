package multitier.trans.controllers;

import jakarta.validation.Valid;
import multitier.trans.dto.AddRouteTimetableEntriesRequest;
import multitier.trans.dto.CreateRouteTimetableRequest;
import multitier.trans.dto.RouteTimetableResponse;
import multitier.trans.service.TimetableService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/routes/{routeId}/timetables")
public class RouteTimetableController {

    private final TimetableService timetableService;

    public RouteTimetableController(TimetableService timetableService) {
        this.timetableService = timetableService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RouteTimetableResponse> createTimetable(@PathVariable Long routeId,
                                                                  @Valid @RequestBody CreateRouteTimetableRequest request) {
        RouteTimetableResponse response = timetableService.createTimetable(routeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<RouteTimetableResponse> getTimetables(@PathVariable Long routeId) {
        return timetableService.getTimetablesForRoute(routeId);
    }

    @PostMapping("/{timetableId}/entries")
    @PreAuthorize("hasRole('ADMIN')")
    public RouteTimetableResponse addEntries(@PathVariable Long routeId,
                                             @PathVariable Long timetableId,
                                             @Valid @RequestBody AddRouteTimetableEntriesRequest request) {
        return timetableService.addEntries(routeId, timetableId, request);
    }
}

