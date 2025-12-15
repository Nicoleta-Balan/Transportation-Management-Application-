import type { Route } from '../types/Route';
import { useTableFilters } from './useTableFilters';

// Helper to get origin station from route
const getOriginStation = (route: Route) => {
    if (route.routeStops && route.routeStops.length > 0) {
        return route.routeStops[0].station;
    }
    return route.originStation;
};

// Helper to get destination station from route
const getDestinationStation = (route: Route) => {
    if (route.routeStops && route.routeStops.length > 0) {
        const stops = route.routeStops;
        return stops[stops.length - 1].station;
    }
    return route.destinationStation;
};

export type RouteSortColumn = 'start' | 'end' | 'distance' | 'duration';

// Helper to get all station names from route
const getAllStationNames = (route: Route): string => {
    if (route.routeStops && route.routeStops.length > 0) {
        return route.routeStops
            .map(stop => stop.station?.name || '')
            .filter(name => name.length > 0)
            .join(' ');
    }
    // Fallback for backward compatibility
    const stations: string[] = [];
    if (route.originStation?.name) stations.push(route.originStation.name);
    if (route.destinationStation?.name) stations.push(route.destinationStation.name);
    return stations.join(' ');
};

export function useRouteFilters(routes: Route[]) {
    const filters = useTableFilters<Route, RouteSortColumn>(routes, {
        searchFields: [
            { 
                name: 'allStations', 
                extract: (route) => getAllStationNames(route)
            },
            { 
                name: 'start', 
                extract: (route) => getOriginStation(route)?.name 
            },
            { 
                name: 'end', 
                extract: (route) => getDestinationStation(route)?.name 
            },
            { 
                name: 'vehicleClass', 
                extract: (route) => route.vehicleClass 
            },
            { 
                name: 'description', 
                extract: (route) => route.description 
            },
        ],
        sortColumns: [
            { 
                key: 'start', 
                extract: (route) => getOriginStation(route)?.name || '',
                type: 'string'
            },
            { 
                key: 'end', 
                extract: (route) => getDestinationStation(route)?.name || '',
                type: 'string'
            },
            { 
                key: 'distance', 
                extract: (route) => route.distance ?? 0,
                type: 'number'
            },
            { 
                key: 'duration', 
                extract: (route) => route.durationMinutes ?? 0,
                type: 'number'
            },
        ],
    });

    return {
        searchTerm: filters.searchTerm,
        setSearchTerm: filters.setSearchTerm,
        sortColumn: filters.sortColumn,
        sortDirection: filters.sortDirection,
        handleSort: filters.handleSort,
        sortedAndFilteredRoutes: filters.filteredData,
    };
}

