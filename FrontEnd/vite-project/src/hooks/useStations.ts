import { useState, useEffect } from 'react';

import type { Station } from '../types/Station';

import { stationApi } from '../services/stationApi';

import { getErrorMessage } from '../utils/errorUtils';

interface StationsState {
    loading: boolean;
    error: string | null;
    deleting: number | null;
    deleteError: Record<number, string>;
}

export function useStations() {
    const [stations, setStations] = useState<Station[]>([]);
    const [stationsState, setStationsState] = useState<StationsState>({
        loading: false,
        error: null,
        deleting: null,
        deleteError: {},
    });

    const loadStations = async () => {
        setStationsState(prev => ({ ...prev, loading: true, error: null }));
        try {
            const data = await stationApi.getAllStations();
            setStations(data);
            setStationsState(prev => ({ ...prev, loading: false }));
        } catch (err) {
            setStationsState(prev => ({
                ...prev,
                loading: false,
                error: getErrorMessage(err, 'Failed to load stations'),
            }));
        }
    };

    const handleDeleteClick = (station: Station) => {
        if (window.confirm(`Are you sure you want to delete station "${station.name}"? This action cannot be undone.`)) {
            handleDeleteConfirm(station.id);
        }
    };

    const handleDeleteConfirm = async (id: number) => {
        setStationsState(prev => ({
            ...prev,
            deleting: id,
            deleteError: (() => {
                const newErrors = { ...prev.deleteError };
                delete newErrors[id];
                return newErrors;
            })(),
        }));
        try {
            await stationApi.deleteStation(id);
            await loadStations(); // Refresh the stations list
            // Clear error for this station on success
            setStationsState(prev => {
                const newErrors = { ...prev.deleteError };
                delete newErrors[id];
                return {
                    ...prev,
                    deleting: null,
                    deleteError: newErrors,
                };
            });
        } catch (err) {
            const errorMessage = getErrorMessage(err, 'Failed to delete station');
            setStationsState(prev => ({
                ...prev,
                deleting: null,
                deleteError: {
                    ...prev.deleteError,
                    [id]: errorMessage,
                },
            }));
        }
    };

    // Fetch stations on mount
    useEffect(() => {
        loadStations();
    }, []);

    return {
        stations,
        setStations,
        loading: stationsState.loading,
        error: stationsState.error,
        loadStations,
        deleting: stationsState.deleting,
        deleteError: stationsState.deleteError,
        handleDeleteClick,
        handleDeleteConfirm,
    };
}

