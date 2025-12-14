import type { Station } from '../types/Station';
import { useResource } from './useResource';
import { stationApi } from '../services/stationApi';

export function useStations() {
    const resource = useResource<Station>(stationApi, {
        resourceName: 'station',
        getDeleteMessage: (station) => 
            `Are you sure you want to delete station "${station.name}"? This action cannot be undone.`,
    });

    return {
        stations: resource.data,
        setStations: resource.setData,
        loading: resource.loading,
        error: resource.error,
        loadStations: resource.load,
        deleting: resource.deleting,
        deleteError: resource.deleteError,
        handleDeleteClick: resource.handleDeleteClick,
        handleDeleteConfirm: resource.handleDeleteConfirm,
    };
}

