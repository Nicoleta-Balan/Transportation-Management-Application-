import type { Station } from '../types/Station';
import { useTableFilters } from './useTableFilters';

export type StationSortColumn = 'name' | 'description' | 'address' | 'status';

export function useStationFilters(stations: Station[]) {
    const filters = useTableFilters<Station, StationSortColumn>(stations, {
        searchFields: [
            { 
                name: 'name', 
                extract: (station) => station.name 
            },
            { 
                name: 'description', 
                extract: (station) => station.description 
            },
            { 
                name: 'address', 
                extract: (station) => station.address 
            },
        ],
        sortColumns: [
            { 
                key: 'name', 
                extract: (station) => station.name,
                type: 'string'
            },
            { 
                key: 'description', 
                extract: (station) => station.description || '',
                type: 'string'
            },
            { 
                key: 'address', 
                extract: (station) => station.address || '',
                type: 'string'
            },
            { 
                key: 'status', 
                extract: (station) => station.status,
                type: 'string'
            },
        ],
    });

    return {
        searchTerm: filters.searchTerm,
        setSearchTerm: filters.setSearchTerm,
        sortColumn: filters.sortColumn,
        sortDirection: filters.sortDirection,
        handleSort: filters.handleSort,
        sortedAndFilteredStations: filters.filteredData,
    };
}

