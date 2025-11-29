package multitier.trans.controllers;

import multitier.trans.dto.CreateStationRequest;
import multitier.trans.model.Station;
import multitier.trans.service.StationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/stations")
public class StationController {

    private final StationService stationService;

    @Autowired
    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    // @Valid triggers the validation rules (@Size, @NotNull) in the CreateStationRequest DTO
    // If validation fails => 400 Bad Request
    @PostMapping
    public ResponseEntity<Station> createStation(@Valid @RequestBody CreateStationRequest request) {
        Station createdStation = stationService.createStation(request);
        // Return 201 Created
        return ResponseEntity.status(201).body(createdStation);
    }

    @GetMapping
    public List<Station> getAllStations() {
        return stationService.getAllStations();
    }
}