import type { Station } from '../types/Station';

export interface RouteValidationResult {
    isValid: boolean;
    error: string | null;
}

export function validateRouteData(
    selectedStations: Station[],
    distance: number | undefined,
    durationMinutes: number | undefined
): RouteValidationResult {
    // Validate selected stations
    if (selectedStations.length < 2) {
        return {
            isValid: false,
            error: 'Please select at least 2 stations',
        };
    }

    // Validate distance
    if (!distance || distance <= 0) {
        return {
            isValid: false,
            error: 'Distance must be greater than 0',
        };
    }

    // Validate duration
    if (!durationMinutes || durationMinutes <= 0) {
        return {
            isValid: false,
            error: 'Duration must be greater than 0',
        };
    }

    return {
        isValid: true,
        error: null,
    };
}

export function normalizeDescription(description: string | undefined): string | undefined {
    return description && description.trim() ? description.trim() : undefined;
}

