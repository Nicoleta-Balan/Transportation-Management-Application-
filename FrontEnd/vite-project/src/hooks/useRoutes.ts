import type { Route } from '../types/Route';
import { useResource } from './useResource';
import { routeApi } from '../services/routeApi';

export function useRoutes() {
    const resource = useResource<Route>(routeApi, {
        resourceName: 'route',
        getDeleteMessage: (route) => {
            // Extract origin and destination from route stops
            const origin = route.routeStops && route.routeStops.length > 0 
                ? route.routeStops[0].station 
                : route.originStation;
            const destination = route.routeStops && route.routeStops.length > 0
                ? route.routeStops[route.routeStops.length - 1].station
                : route.destinationStation;
            const startName = origin?.name || 'Unknown';
            const endName = destination?.name || 'Unknown';
            
            return `Are you sure you want to delete route from "${startName}" to "${endName}"? This action cannot be undone.`;
        },
    });

    return {
        routes: resource.data,
        setRoutes: resource.setData,
        loading: resource.loading,
        error: resource.error,
        loadRoutes: resource.load,
        deleting: resource.deleting,
        deleteError: resource.deleteError,
        handleDeleteClick: resource.handleDeleteClick,
        handleDeleteConfirm: resource.handleDeleteConfirm,
    };
}

