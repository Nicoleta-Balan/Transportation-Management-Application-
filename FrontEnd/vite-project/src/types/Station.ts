export const StationStatus = {
    ACTIVE: 'ACTIVE',
    INACTIVE: 'INACTIVE',
    MAINTENANCE: 'MAINTENANCE',
} as const;

export type StationStatus = typeof StationStatus[keyof typeof StationStatus];

// Matches the backend Station entity from model
export interface Station{
    id: number;
    name: string;
    description?: string;
    address: string;
    latitude: number;
    longitude: number;
    status: StationStatus;
}
// Matches the backend DTO
export interface CreateStationRequest{
    name: string;
    description?: string;
    address: string;
    latitude: number;
    longitude: number;
    status: StationStatus;
}

export  interface UpdateStationRequest{
    description?: string;
    address: string;
    latitude: number;
    longitude: number;
    status: StationStatus;
}

/**
 * Common fields required for location handling in forms
 * Used to create type-safe constraints for form hooks
 */
export interface LocationFields {
    address: string;
    latitude: number;
    longitude: number;
}