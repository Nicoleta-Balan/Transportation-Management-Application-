package multitier.trans.utils;

import multitier.trans.dto.RouteStopRequest;
import multitier.trans.exception.ValidationException;
import multitier.trans.model.Route;
import multitier.trans.model.RouteStop;
import multitier.trans.model.Station;

public class RouteStopCreationStrategy implements StopCreationStrategy<RouteStop, RouteStopRequest, Route> {

    @Override
    public RouteStop createStopEntity() {
        return new RouteStop();
    }

    @Override
    public void setParent(RouteStop stop, Route route) {
        stop.setRoute(route);
    }

    @Override
    public void setCommonFields(RouteStop stop, Station station, Integer sequenceOrder) {
        stop.setStation(station);
        stop.setSequenceOrder(sequenceOrder);
    }

    @Override
    public RouteStop setSpecificFields(RouteStop stop, RouteStopRequest request, int index, RouteStop previousStop) {
        // Set distance and duration from previous
        stop.setDistanceFromPrevious(request.getDistanceFromPrevious());
        stop.setDurationMinutesFromPrevious(request.getDurationMinutesFromPrevious());

        // Calculate cumulative values
        double cumulativeDistance = request.getDistanceFromPrevious();
        int cumulativeDuration = request.getDurationMinutesFromPrevious();

        if (previousStop != null) {
            cumulativeDistance += previousStop.getCumulativeDistance();
            cumulativeDuration += previousStop.getCumulativeDurationMinutes();
        }

        stop.setCumulativeDistance(cumulativeDistance);
        stop.setCumulativeDurationMinutes(cumulativeDuration);

        return stop;
    }

    @Override
    public void validateRequest(RouteStopRequest request) {
        if (request.getStationId() == null) {
            throw new ValidationException("Station ID cannot be null for route stop");
        }
        if (request.getSequenceOrder() == null) {
            throw new ValidationException("Sequence order cannot be null for route stop");
        }
        if (request.getDistanceFromPrevious() == null) {
            throw new ValidationException("Distance from previous cannot be null for route stop");
        }
        if (request.getDurationMinutesFromPrevious() == null) {
            throw new ValidationException("Duration from previous cannot be null for route stop");
        }
    }
}

