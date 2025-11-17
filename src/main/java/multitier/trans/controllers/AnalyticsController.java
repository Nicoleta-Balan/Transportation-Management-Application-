package multitier.trans.controllers;

import multitier.trans.model.RevenueSummary;
import multitier.trans.model.RouteAvailability;
import multitier.trans.model.RouteStatistics;
import multitier.trans.service.AnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/routes/{routeId}/availability")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RouteAvailability> getRouteAvailability(@PathVariable Long routeId) {
        return ResponseEntity.ok(analyticsService.getRouteAvailability(routeId));
    }

    @GetMapping("/routes/{routeId}/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RouteStatistics> getRouteStatistics(@PathVariable Long routeId) {
        return ResponseEntity.ok(analyticsService.getRouteStatistics(routeId));
    }

    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RevenueSummary>> getRevenueSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long routeId,
            @RequestParam(required = false) String passengerCategory,
            @RequestParam(required = false) String vehicleClass) {

        return ResponseEntity.ok(
                analyticsService.getRevenueSummary(date, endDate, routeId, passengerCategory, vehicleClass)
        );
    }
}

