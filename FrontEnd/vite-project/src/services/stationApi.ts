import type { Station, CreateStationRequest, UpdateStationRequest } from '../types/Station';
import { createResourceApi } from '../utils/createResourceApi';

// Create base API with standard CRUD operations
const baseStationApi = createResourceApi<Station, CreateStationRequest, UpdateStationRequest>({
    resourceName: 'stations',
    legacyMethods: {
        getAll: 'getAllStations',
        delete: 'deleteStation',
    },
});

// Add station-specific methods with legacy names
export const stationApi = {
    ...baseStationApi,
    createStation: baseStationApi.create,
    updateStation: baseStationApi.update,
};