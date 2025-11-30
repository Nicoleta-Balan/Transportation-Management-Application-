import { useState, useMemo } from 'react';

import type { Station } from '../types/Station';

export function useStationFilters(stations: Station[]) {
    const [searchTerm, setSearchTerm] = useState('');
    const [sortColumn, setSortColumn] = useState<keyof Station | null>(null);
    const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');

    const handleSort = (column: keyof Station) => {
        if (sortColumn === column) {
            setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
        } else {
            setSortColumn(column);
            setSortDirection('asc');
        }
    };

    const sortedAndFilteredStations = useMemo(() => {
        return stations
            .filter((station) => {
                if (!searchTerm) return true;
                const search = searchTerm.toLowerCase();
                return (
                    station.name.toLowerCase().includes(search) ||
                    (station.description && station.description.toLowerCase().includes(search)) ||
                    (station.address && station.address.toLowerCase().includes(search))
                );
            })
            .sort((a, b) => {
                if (!sortColumn) return 0;
                
                let aValue: string | number | undefined = a[sortColumn];
                let bValue: string | number | undefined = b[sortColumn];
                
                // Handle undefined/null values
                if (aValue === undefined || aValue === null) aValue = '';
                if (bValue === undefined || bValue === null) bValue = '';
                
                // Convert to string for comparison
                const aStr = String(aValue).toLowerCase();
                const bStr = String(bValue).toLowerCase();
                
                if (sortDirection === 'asc') {
                    return aStr.localeCompare(bStr);
                } else {
                    return bStr.localeCompare(aStr);
                }
            });
    }, [stations, searchTerm, sortColumn, sortDirection]);

    return {
        searchTerm,
        setSearchTerm,
        sortColumn,
        sortDirection,
        handleSort,
        sortedAndFilteredStations,
    };
}

