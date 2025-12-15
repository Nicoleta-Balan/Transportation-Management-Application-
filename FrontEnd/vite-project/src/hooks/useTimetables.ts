import type { Timetable } from '../types/Timetable';
import { useResource } from './useResource';
import { timetableApi } from '../services/timetableApi';

export function useTimetables(routeId: number | null) {
    const resource = useResource<Timetable>(timetableApi, {
        resourceName: 'timetable',
        routeId,
        loadOnMount: false, // Load manually via useEffect in TimetablePanel
        normalizeData: (timetables) => {
            // Ensure timetables have timetableStops array initialized
            return timetables.map(t => ({
                ...t,
                timetableStops: t.timetableStops || []
            }));
        },
        getDeleteMessage: () => 
            'Are you sure you want to delete this timetable? This action cannot be undone.',
    });

    return {
        timetables: resource.data,
        loading: resource.loading,
        error: resource.error,
        deleting: resource.deleting,
        deleteError: resource.deleteError,
        loadTimetables: resource.load,
        handleDeleteClick: resource.handleDeleteClick,
    };
}

