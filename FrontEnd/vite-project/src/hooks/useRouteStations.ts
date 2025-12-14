import { useState, useCallback } from 'react';
import type { Station } from '../types/Station';
import { handleRouteStationSelect } from '../utils/routeStationSelection';

export function useRouteStations(initialStations?: Station[]) {
    const [selectedStations, setSelectedStations] = useState<Station[]>(initialStations || []);

    const handleStationSelect = useCallback((station: Station) => {
        setSelectedStations(prev => handleRouteStationSelect(station, prev));
    }, []);

    const clearSelection = useCallback(() => {
        setSelectedStations([]);
    }, []);

    const setStations = useCallback((stations: Station[]) => {
        setSelectedStations(stations);
    }, []);

    return {
        selectedStations,
        handleStationSelect,
        clearSelection,
        setStations,
    };
}

