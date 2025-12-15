import { useState, useMemo, useCallback } from 'react';

export interface SearchField<T> {
    name: string;
    extract: (item: T) => string | null | undefined;
}

export interface SortColumn<T, K extends string = string> {
    key: K;
    extract: (item: T) => string | number | null | undefined;
    type?: 'string' | 'number';
}

interface UseTableFiltersOptions<T, K extends string> {
    searchFields: SearchField<T>[];
    sortColumns: SortColumn<T, K>[];
    initialSortColumn?: K;
    initialSortDirection?: 'asc' | 'desc';
}

export function useTableFilters<T, K extends string = string>(
    data: T[],
    options: UseTableFiltersOptions<T, K>
) {
    const {
        searchFields,
        sortColumns,
        initialSortColumn,
        initialSortDirection = 'asc',
    } = options;

    // State
    const [searchTerm, setSearchTerm] = useState('');
    const [sortColumn, setSortColumn] = useState<K | null>(initialSortColumn ?? null);
    const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>(initialSortDirection);

    const handleSort = useCallback((column: K) => {
        if (sortColumn === column) {
            setSortDirection(prev => prev === 'asc' ? 'desc' : 'asc');
        } else {
            setSortColumn(column);
            setSortDirection('asc');
        }
    }, [sortColumn]);

    const filteredData = useMemo(() => {
        return data
            // Filter by search term
            .filter((item) => {
                if (!searchTerm) return true;
                
                const search = searchTerm.toLowerCase();
                
                // Check if any search field matches
                return searchFields.some((field) => {
                    const value = field.extract(item);
                    return value && value.toLowerCase().includes(search);
                });
            })
            // Sort by selected column
            .sort((a, b) => {
                if (!sortColumn) return 0;
                
                // Find the sort column configuration
                const sortConfig = sortColumns.find(col => col.key === sortColumn);
                if (!sortConfig) return 0;
                
                // Extract values
                let aValue = sortConfig.extract(a);
                let bValue = sortConfig.extract(b);
                
                // Handle null/undefined
                if (aValue === null || aValue === undefined) aValue = sortConfig.type === 'number' ? 0 : '';
                if (bValue === null || bValue === undefined) bValue = sortConfig.type === 'number' ? 0 : '';
                
                // Compare based on type
                if (sortConfig.type === 'number') {
                    const aNum = Number(aValue);
                    const bNum = Number(bValue);
                    return sortDirection === 'asc' ? aNum - bNum : bNum - aNum;
                } else {
                    // String comparison
                    const aStr = String(aValue).toLowerCase();
                    const bStr = String(bValue).toLowerCase();
                    return sortDirection === 'asc' 
                        ? aStr.localeCompare(bStr) 
                        : bStr.localeCompare(aStr);
                }
            });
    }, [data, searchTerm, sortColumn, sortDirection, searchFields, sortColumns]);

    return {
        // Search state
        searchTerm,
        setSearchTerm,
        
        // Sort state
        sortColumn,
        sortDirection,
        handleSort,
        
        // Filtered/sorted data
        filteredData,
    };
}

