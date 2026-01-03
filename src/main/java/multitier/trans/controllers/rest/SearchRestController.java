package multitier.trans.controllers.rest;

import multitier.trans.model.Timetable;
import multitier.trans.service.TimetableService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/search-api") // Moved out of /api to avoid Spring Data REST conflict
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SearchRestController {

    private static final Logger logger = LoggerFactory.getLogger(SearchRestController.class);

    private final TimetableService timetableService;

    @GetMapping("/timetables")
    public ResponseEntity<List<Timetable>> searchTimetables(
            @RequestParam Long fromStationId,
            @RequestParam Long toStationId,
            @RequestParam LocalDate date) {
        
        logger.info("SEARCH ENDPOINT HIT: from={}, to={}, date={}", fromStationId, toStationId, date);
        List<Timetable> timetables = timetableService.searchTimetables(fromStationId, toStationId, date);
        logger.info("SEARCH ENDPOINT FOUND {} timetables", timetables.size());
        return ResponseEntity.ok(timetables);
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        logger.info("SEARCH TEST ENDPOINT HIT");
        return ResponseEntity.ok("Search API is running.");
    }
}
