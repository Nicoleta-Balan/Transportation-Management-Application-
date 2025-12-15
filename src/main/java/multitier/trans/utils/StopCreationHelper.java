package multitier.trans.utils;

import multitier.trans.model.Station;
import multitier.trans.repository.StationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class StopCreationHelper<TStop, TRequest, TParent> {

    private final StopCreationStrategy<TStop, TRequest, TParent> strategy;
    private final StationRepository stationRepository;
    private final Function<TRequest, Long> stationIdExtractor;
    private final Function<TRequest, Integer> sequenceOrderExtractor;

    public StopCreationHelper(
            StopCreationStrategy<TStop, TRequest, TParent> strategy,
            StationRepository stationRepository,
            Function<TRequest, Long> stationIdExtractor,
            Function<TRequest, Integer> sequenceOrderExtractor) {
        this.strategy = strategy;
        this.stationRepository = stationRepository;
        this.stationIdExtractor = stationIdExtractor;
        this.sequenceOrderExtractor = sequenceOrderExtractor;
    }

    public List<TStop> createStops(TParent parent, List<TRequest> stopRequests) {
        List<TStop> stops = new ArrayList<>();
        TStop previousStop = null;

        for (int i = 0; i < stopRequests.size(); i++) {
            TRequest stopRequest = stopRequests.get(i);

            // Validate request
            strategy.validateRequest(stopRequest);

            // Find station
            Long stationId = stationIdExtractor.apply(stopRequest);
            Station station = RepositoryUtils.findByIdOrThrow(
                    stationRepository.findById(stationId),
                    "Station",
                    stationId
            );

            // Create stop entity
            TStop stop = strategy.createStopEntity();

            // Set parent
            strategy.setParent(stop, parent);

            // Set common fields (station, sequence order)
            Integer sequenceOrder = sequenceOrderExtractor.apply(stopRequest);
            strategy.setCommonFields(stop, station, sequenceOrder);

            // Set strategy-specific fields (may include cumulative calculations)
            stop = strategy.setSpecificFields(stop, stopRequest, i, previousStop);

            stops.add(stop);
            previousStop = stop; // For cumulative calculations in next iteration
        }

        return stops;
    }
}

