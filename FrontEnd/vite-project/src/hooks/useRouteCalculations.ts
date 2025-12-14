import { useMemo } from 'react';
import type { Station } from '../types/Station';
import type { SegmentOverride } from '../types/Route';
import { calculateDistance, calculateDuration } from '../utils/distanceCalculator';
import { DISTANCE_DECIMAL_PLACES } from '../constants/calculationConstants';

export function useRouteCalculations(
    selectedStations: Station[],
    segmentOverrides?: Map<number, SegmentOverride>
) {
    const { calculatedDistance, calculatedDurationMinutes } = useMemo(() => {
        if (selectedStations.length < 2) {
            return { calculatedDistance: 0, calculatedDurationMinutes: 0 };
        }

        let totalDistance = 0;
        let totalDuration = 0;

        for (let i = 1; i < selectedStations.length; i++) {
            const override = segmentOverrides?.get(i);

            if (override) {
                totalDistance += override.distance;
                totalDuration += override.duration;
            } else {
                const currentStation = selectedStations[i];
                const previousStation = selectedStations[i - 1];

                if (currentStation.latitude && currentStation.longitude && 
                    previousStation.latitude && previousStation.longitude) {
                    const segmentDistance = calculateDistance(
                        previousStation.latitude,
                        previousStation.longitude,
                        currentStation.latitude,
                        currentStation.longitude
                    );
                    const segmentDurationHours = calculateDuration(segmentDistance);
                    const segmentDurationMinutes = Math.round(segmentDurationHours * 60);

                    totalDistance += segmentDistance;
                    totalDuration += segmentDurationMinutes;
                }
            }
        }

        const decimalMultiplier = Math.pow(10, DISTANCE_DECIMAL_PLACES);
        return {
            calculatedDistance: Math.round(totalDistance * decimalMultiplier) / decimalMultiplier,
            calculatedDurationMinutes: totalDuration,
        };
    }, [selectedStations, segmentOverrides]);

    return { calculatedDistance, calculatedDurationMinutes };
}

