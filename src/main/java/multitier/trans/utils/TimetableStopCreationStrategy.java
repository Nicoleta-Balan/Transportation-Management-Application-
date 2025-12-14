package multitier.trans.utils;

import multitier.trans.dto.TimetableStopRequest;
import multitier.trans.model.Station;
import multitier.trans.model.Timetable;
import multitier.trans.model.TimetableStop;

public class TimetableStopCreationStrategy implements StopCreationStrategy<TimetableStop, TimetableStopRequest, Timetable> {

    @Override
    public TimetableStop createStopEntity() {
        return new TimetableStop();
    }

    @Override
    public void setParent(TimetableStop stop, Timetable timetable) {
        stop.setTimetable(timetable);
    }

    @Override
    public void setCommonFields(TimetableStop stop, Station station, Integer sequenceOrder) {
        stop.setStation(station);
        stop.setSequenceOrder(sequenceOrder);
    }

    @Override
    public TimetableStop setSpecificFields(TimetableStop stop, TimetableStopRequest request, int index, TimetableStop previousStop) {
        // Set arrival and departure times
        stop.setArrivalTime(request.getArrivalTime());
        stop.setDepartureTime(request.getDepartureTime());

        return stop;
    }

    @Override
    public void validateRequest(TimetableStopRequest request) {
        // TimetableStopRequest validation is handled by Jakarta validation annotations
        // No additional validation needed here
    }
}

