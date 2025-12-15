package multitier.trans.utils;

import multitier.trans.model.Station;

public interface StopCreationStrategy<TStop, TRequest, TParent> {

    TStop createStopEntity();

    void setParent(TStop stop, TParent parent);

    void setCommonFields(TStop stop, Station station, Integer sequenceOrder);

    TStop setSpecificFields(TStop stop, TRequest request, int index, TStop previousStop);

    void validateRequest(TRequest request);
}

