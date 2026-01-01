import type { Route, CreateRouteRequest, UpdateRouteRequest } from '../types/Route';
import { createResourceApi } from '../utils/createResourceApi';
import { fetchWithErrorHandling } from '../utils/apiClient';
import { API_CONFIG } from '../config/api';

// Create base API with standard CRUD operations
const baseRouteApi = createResourceApi<Route, CreateRouteRequest, UpdateRouteRequest>({
    resourceName: 'routes',
    legacyMethods: {
        getAll: 'getAllRoutes',
        delete: 'deleteRoute',
    },
});

// Add route-specific methods with legacy names
export const routeApi = {
    ...baseRouteApi,
    
    // Override getAll() to use search endpoint that eagerly loads routeStops
    // Spring Data REST's default GET /api/routes doesn't load routeStops (LAZY fetch)
    // Use /api/routes/search/allWithStops to get routes with stops loaded
    async getAll(): Promise<Route[]> {
        const response = await fetchWithErrorHandling<Route[]>(
            `${API_CONFIG.BASE_URL}/api/routes/search/allWithStops`
        );
        return Array.isArray(response) ? response : [];
    },
    
    // Override getById() to use search endpoint that eagerly loads routeStops
    // Spring Data REST's default GET /api/routes/{id} doesn't load routeStops (LAZY fetch)
    // Use /api/routes/search/findByIdWithStops?id={id} to get route with stops loaded
    async getById(id: number): Promise<Route> {
        return fetchWithErrorHandling<Route>(
            `${API_CONFIG.BASE_URL}/api/routes/search/findByIdWithStops?id=${id}`
        );
    },

    // Search routes by origin and destination
    async searchRoutesByStations(originId: number, destinationId: number): Promise<Route[]> {
        const response = await fetchWithErrorHandling<{ _embedded?: { routes: Route[] } } | Route[]>(
            `${API_CONFIG.BASE_URL}/api/routes/search/byStations?originId=${originId}&destinationId=${destinationId}`
        );
        
        // Handle Spring Data REST response format (HAL) or direct array
        if (Array.isArray(response)) {
            return response;
        }
        return response._embedded?.routes || [];
    },
    
    getRouteById: function(id: number) {
        return this.getById(id);
    },
    createRoute: baseRouteApi.create,
    updateRoute: baseRouteApi.update,
};
