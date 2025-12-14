import type { Station } from '../types/Station';

export function handleRouteStationSelect(
    station: Station,
    currentStations: Station[],
    allowInactive: boolean = false
): Station[] {
    // Only allow ACTIVE stations unless explicitly allowed
    if (!allowInactive && station.status !== 'ACTIVE') {
        return currentStations;
    }

    // If station is already selected, remove it
    const index = currentStations.findIndex(s => s.id === station.id);
    if (index !== -1) {
        return currentStations.filter(s => s.id !== station.id);
    }

    // Add station to selection
    return [...currentStations, station];
}

