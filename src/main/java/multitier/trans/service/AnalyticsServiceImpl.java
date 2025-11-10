package multitier.trans.service;

import multitier.trans.model.RevenueSummary;
import multitier.trans.model.RouteAvailability;
import multitier.trans.model.RouteStatistics;
import multitier.trans.repository.RevenueSummaryRepository;
import multitier.trans.repository.RouteAvailabilityRepository;
import multitier.trans.repository.RouteStatisticsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final RouteAvailabilityRepository routeAvailabilityRepository;
    private final RouteStatisticsRepository routeStatisticsRepository;
    private final RevenueSummaryRepository revenueSummaryRepository;

    public AnalyticsServiceImpl(RouteAvailabilityRepository routeAvailabilityRepository,
                                RouteStatisticsRepository routeStatisticsRepository,
                                RevenueSummaryRepository revenueSummaryRepository) {
        this.routeAvailabilityRepository = routeAvailabilityRepository;
        this.routeStatisticsRepository = routeStatisticsRepository;
        this.revenueSummaryRepository = revenueSummaryRepository;
    }

    @Override
    public RouteAvailability getRouteAvailability(Long routeId) {
        return routeAvailabilityRepository.findById(routeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Route availability not found for route " + routeId));
    }

    @Override
    public RouteStatistics getRouteStatistics(Long routeId) {
        return routeStatisticsRepository.findById(routeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Route statistics not found for route " + routeId));
    }

    @Override
    public List<RevenueSummary> getRevenueSummary(LocalDate summaryDate,
                                                  LocalDate endDate,
                                                  Long routeId,
                                                  String passengerCategory,
                                                  String vehicleClass) {
        List<RevenueSummary> summaries;

        if (summaryDate != null && endDate != null) {
            summaries = revenueSummaryRepository.findBySummaryDateBetween(summaryDate, endDate);
        } else if (summaryDate != null) {
            summaries = revenueSummaryRepository.findBySummaryDate(summaryDate);
        } else {
            summaries = revenueSummaryRepository.findAll();
        }

        return summaries.stream()
                .filter(summary -> routeId == null || Objects.equals(summary.getRouteId(), routeId))
                .filter(summary -> passengerCategory == null ||
                        (summary.getPassengerCategory() != null &&
                                summary.getPassengerCategory().equalsIgnoreCase(passengerCategory)))
                .filter(summary -> vehicleClass == null ||
                        (summary.getVehicleClass() != null &&
                                summary.getVehicleClass().equalsIgnoreCase(vehicleClass)))
                .collect(Collectors.toList());
    }
}

