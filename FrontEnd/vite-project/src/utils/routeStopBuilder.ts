import type { Station } from '../types/Station';
import type { RouteStopRequest } from '../types/Route';
import { calculateDistance, calculateDuration } from './distanceCalculator';
import { DISTANCE_DECIMAL_PLACES } from '../constants/calculationConstants';

type SegmentOverrideValue = {
    distance: number;
    duration: number;
};

export function buildRouteStops(
    selectedStations: Station[],
    segmentOverrides: Map<number, SegmentOverrideValue>
): RouteStopRequest[] {
    return selectedStations.map((station, index) => {
        let distanceFromPrevious = 0;
        let durationMinutesFromPrevious = 0;

        if (index > 0) {
            // Check if there's a manual override for this segment
            const override = segmentOverrides.get(index);
            
            if (override) {
                // Use manually edited values
                distanceFromPrevious = override.distance;
                durationMinutesFromPrevious = override.duration;
            } else {
                // Use auto-calculated values
                const previousStation = selectedStations[index - 1];
                if (station.latitude && station.longitude && 
                    previousStation.latitude && previousStation.longitude) {
                    distanceFromPrevious = calculateDistance(
                        previousStation.latitude,
                        previousStation.longitude,
                        station.latitude,
                        station.longitude
                    );
                    const durationHours = calculateDuration(distanceFromPrevious);
                    durationMinutesFromPrevious = Math.round(durationHours * 60);
                }
            }
        }

        return {
            stationId: station.id,
            sequenceOrder: index,
            distanceFromPrevious: parseFloat(distanceFromPrevious.toFixed(DISTANCE_DECIMAL_PLACES)),
            durationMinutesFromPrevious,
        };
    });
}

