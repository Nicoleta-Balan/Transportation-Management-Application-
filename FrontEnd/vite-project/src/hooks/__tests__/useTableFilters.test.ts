import { describe, it, expect } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useTableFilters } from '../useTableFilters';

interface TestItem {
    id: number;
    name: string;
    value: number;
}

describe('useTableFilters', () => {
    const testData: TestItem[] = [
        { id: 1, name: 'Apple', value: 10 },
        { id: 2, name: 'Banana', value: 5 },
        { id: 3, name: 'Cherry', value: 15 },
    ];

    it('should filter data by search term', () => {
        const { result } = renderHook(() =>
            useTableFilters(testData, {
                searchFields: [
                    { name: 'name', extract: (item) => item.name },
                ],
                sortColumns: [],
            })
        );

        act(() => {
            result.current.setSearchTerm('banana');
        });

        expect(result.current.filteredData).toEqual([testData[1]]);
    });

    it('should filter across multiple fields', () => {
        const { result } = renderHook(() =>
            useTableFilters(testData, {
                searchFields: [
                    { name: 'name', extract: (item) => item.name },
                    { name: 'value', extract: (item) => item.value.toString() },
                ],
                sortColumns: [],
            })
        );

        act(() => {
            result.current.setSearchTerm('10');
        });

        expect(result.current.filteredData).toEqual([testData[0]]);
    });

    it('should sort by string column ascending', () => {
        const { result } = renderHook(() =>
            useTableFilters(testData, {
                searchFields: [],
                sortColumns: [
                    { key: 'name', extract: (item) => item.name, type: 'string' },
                ],
            })
        );

        act(() => {
            result.current.handleSort('name');
        });

        expect(result.current.filteredData[0].name).toBe('Apple');
        expect(result.current.filteredData[1].name).toBe('Banana');
        expect(result.current.filteredData[2].name).toBe('Cherry');
    });

    it('should sort by string column descending', () => {
        const { result } = renderHook(() =>
            useTableFilters(testData, {
                searchFields: [],
                sortColumns: [
                    { key: 'name', extract: (item) => item.name, type: 'string' },
                ],
            })
        );

        act(() => {
            result.current.handleSort('name');
        });

        expect(result.current.sortDirection).toBe('asc');

        act(() => {
            result.current.handleSort('name'); // Toggle to descending
        });

        expect(result.current.sortDirection).toBe('desc');
        expect(result.current.filteredData[0].name).toBe('Cherry');
        expect(result.current.filteredData[2].name).toBe('Apple');
    });

    it('should sort by number column', () => {
        const { result } = renderHook(() =>
            useTableFilters(testData, {
                searchFields: [],
                sortColumns: [
                    { key: 'value', extract: (item) => item.value, type: 'number' },
                ],
            })
        );

        act(() => {
            result.current.handleSort('value');
        });

        expect(result.current.filteredData[0].value).toBe(5);
        expect(result.current.filteredData[1].value).toBe(10);
        expect(result.current.filteredData[2].value).toBe(15);
    });

    it('should combine search and sort', () => {
        const { result } = renderHook(() =>
            useTableFilters(testData, {
                searchFields: [
                    { name: 'name', extract: (item) => item.name },
                ],
                sortColumns: [
                    { key: 'value', extract: (item) => item.value, type: 'number' },
                ],
            })
        );

        act(() => {
            result.current.setSearchTerm('a'); // Matches Apple and Banana
            result.current.handleSort('value');
        });

        expect(result.current.filteredData.length).toBe(2);
        expect(result.current.filteredData[0].name).toBe('Banana'); // Lower value first
        expect(result.current.filteredData[1].name).toBe('Apple');
    });

    it('should handle empty search term', () => {
        const { result } = renderHook(() =>
            useTableFilters(testData, {
                searchFields: [
                    { name: 'name', extract: (item) => item.name },
                ],
                sortColumns: [],
            })
        );

        act(() => {
            result.current.setSearchTerm('');
        });

        expect(result.current.filteredData).toEqual(testData);
    });

    it('should handle null/undefined values in sort', () => {
        const dataWithNulls: TestItem[] = [
            { id: 1, name: 'Apple', value: 10 },
            { id: 2, name: null as unknown as string, value: 5 },
            { id: 3, name: 'Cherry', value: 15 },
        ];

        const { result } = renderHook(() =>
            useTableFilters(dataWithNulls, {
                searchFields: [],
                sortColumns: [
                    { key: 'name', extract: (item) => item.name, type: 'string' },
                ],
            })
        );

        act(() => {
            result.current.handleSort('name');
        });

        // Should not crash and should handle nulls gracefully
        expect(result.current.filteredData.length).toBe(3);
    });
});

