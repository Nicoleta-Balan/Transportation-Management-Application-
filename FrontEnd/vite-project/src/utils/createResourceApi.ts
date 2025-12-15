import type { ResourceAPI } from '../hooks/useResource';
import { API_CONFIG } from '../config/api';
import { fetchWithErrorHandling, fetchWithoutJson } from './apiClient';

interface ResourceApiConfig {
    /** Resource name (plural) for the API endpoint, e.g., "routes", "stations", "timetables" */
    resourceName: string;
    
    /** Optional custom methods to add to the API object */
    customMethods?: Record<string, (...args: any[]) => any>;
    
    /** Optional legacy method name mappings for backward compatibility */
    legacyMethods?: {
        getAll?: string;
        delete?: string;
    };
}

/**
 * Spring Data REST HAL format response structure
 */
interface HalResponse<T> {
    _embedded?: {
        [key: string]: T[];
    };
    _links?: Record<string, any>;
    page?: {
        size: number;
        totalElements: number;
        totalPages: number;
        number: number;
    };
}

/**
 * Extracts array from Spring Data REST HAL format response.
 * Handles both HAL format (_embedded) and direct array responses.
 */
function extractArrayFromHalResponse<T>(data: any, resourceName: string): T[] {
    // If it's already an array, return it directly
    if (Array.isArray(data)) {
        return data;
    }
    
    // If it's HAL format, extract from _embedded
    if (data && typeof data === 'object' && data._embedded) {
        // Try to find the resource array in _embedded
        // Spring Data REST uses the resource name (plural) as the key
        const embeddedKey = Object.keys(data._embedded).find(key => 
            key.toLowerCase() === resourceName.toLowerCase() ||
            key.toLowerCase() === resourceName.toLowerCase() + 's'
        ) || Object.keys(data._embedded)[0];
        
        if (embeddedKey && Array.isArray(data._embedded[embeddedKey])) {
            return data._embedded[embeddedKey];
        }
    }
    
    // Fallback: return empty array if we can't parse it
    console.warn(`Could not extract array from HAL response for ${resourceName}:`, data);
    return [];
}

export function createResourceApi<T, CreateRequest, UpdateRequest>(
    config: ResourceApiConfig
): ResourceAPI<T> & {
    getById: (id: number) => Promise<T>;
    create: (request: CreateRequest) => Promise<T>;
    update: (id: number, request: UpdateRequest) => Promise<T>;
    delete: (id: number) => Promise<void>;
} & Record<string, any> {
    const { resourceName, customMethods = {}, legacyMethods = {} } = config;
    const baseUrl = `${API_CONFIG.BASE_URL}/api/${resourceName}`;

    // Standard CRUD methods
    const api = {
        // Generic resource methods (for useResource hook)
        async getAll(): Promise<T[]> {
            const response = await fetchWithErrorHandling<HalResponse<T> | T[]>(baseUrl);
            return extractArrayFromHalResponse<T>(response, resourceName);
        },

        async getById(id: number): Promise<T> {
            return fetchWithErrorHandling<T>(`${baseUrl}/${id}`);
        },

        async create(request: CreateRequest): Promise<T> {
            return fetchWithErrorHandling<T>(baseUrl, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(request),
            });
        },

        async update(id: number, request: UpdateRequest): Promise<T> {
            return fetchWithErrorHandling<T>(`${baseUrl}/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(request),
            });
        },

        async delete(id: number): Promise<void> {
            return fetchWithoutJson(`${baseUrl}/${id}`, {
                method: 'DELETE',
            });
        },
    };

    // Add legacy method names for backward compatibility
    if (legacyMethods.getAll) {
        (api as any)[legacyMethods.getAll] = api.getAll;
    }
    if (legacyMethods.delete) {
        (api as any)[legacyMethods.delete] = api.delete;
    }

    // Add custom methods
    Object.assign(api, customMethods);

    return api as any;
}

