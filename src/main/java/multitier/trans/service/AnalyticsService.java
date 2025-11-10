package multitier.trans.service;

import multitier.trans.model.RevenueSummary;
import multitier.trans.model.RouteAvailability;
import multitier.trans.model.RouteStatistics;

import java.time.LocalDate;
import java.util.List;

public interface AnalyticsService {

    RouteAvailability getRouteAvailability(Long routeId);

    RouteStatistics getRouteStatistics(Long routeId);

    List<RevenueSummary> getRevenueSummary(LocalDate summaryDate,
                                           LocalDate endDate,
                                           Long routeId,
                                           String passengerCategory,
                                           String vehicleClass);
}

