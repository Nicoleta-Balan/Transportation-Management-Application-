import { useState, useCallback, useRef, useEffect } from 'react';
import type { Station } from '../types/Station';
import type { SegmentOverride } from '../types/Route';
import { calculateDistance, calculateDuration } from '../utils/distanceCalculator';
import { DISTANCE_DECIMAL_PLACES } from '../constants/calculationConstants';

export function useRouteSegmentOverrides(
    selectedStations: Station[],
    setValue: (name: 'distance' | 'durationMinutes', value: number, options?: { shouldValidate?: boolean }) => void
) {
    const [segmentOverrides, setSegmentOverrides] = useState<Map<number, SegmentOverride>>(new Map());
    
    // Use a ref to always access the latest segmentOverrides in callbacks
    // This fixes the stale closure bug
    const segmentOverridesRef = useRef(segmentOverrides);
    
    // Keep the ref in sync with state
    useEffect(() => {
        segmentOverridesRef.current = segmentOverrides;
    }, [segmentOverrides]);

    const handleSegmentChange = useCallback((stationIndex: number, distance: number, duration: number) => {
        setSegmentOverrides(prev => {
            const newMap = new Map(prev);
            newMap.set(stationIndex, { stationIndex, distance, duration });
            
            // Recalculate total distance and duration with the new override
            let totalDistance = 0;
            let totalDuration = 0;
            
            for (let i = 1; i < selectedStations.length; i++) {
                // Use the new override if this is the station being updated, otherwise use existing
                const override = stationIndex === i ? { distance, duration } : newMap.get(i);
                
                if (override) {
                    totalDistance += override.distance;
                    totalDuration += override.duration;
                } else {
                    // Use calculated values
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
            
            // Update the form fields (round to specified decimal places)
            const decimalMultiplier = Math.pow(10, DISTANCE_DECIMAL_PLACES);
            setValue('distance', Math.round(totalDistance * decimalMultiplier) / decimalMultiplier, { shouldValidate: true });
            setValue('durationMinutes', totalDuration, { shouldValidate: true });
            
            return newMap;
        });
    }, [selectedStations, setValue]);

    /**
     * Clear all segment overrides (used when resetting form).
     */
    const clearSegmentOverrides = useCallback(() => {
        setSegmentOverrides(new Map());
    }, []);

    /**
     * Set segment overrides from existing route data (used when loading a route for editing).
     */
    const setSegmentOverridesFromRoute = useCallback((routeStops: Array<{ distanceFromPrevious: number; durationMinutesFromPrevious: number }>) => {
        const overrides = new Map<number, SegmentOverride>();
        routeStops.forEach((stop, index) => {
            if (index > 0) { // Only for segments, not the first station itself
                overrides.set(index, {
                    stationIndex: index,
                    distance: stop.distanceFromPrevious,
                    duration: stop.durationMinutesFromPrevious,
                });
            }
        });
        setSegmentOverrides(overrides);
    }, []);

    return {
        segmentOverrides,
        segmentOverridesRef,
        handleSegmentChange,
        clearSegmentOverrides,
        setSegmentOverridesFromRoute,
    };
}

