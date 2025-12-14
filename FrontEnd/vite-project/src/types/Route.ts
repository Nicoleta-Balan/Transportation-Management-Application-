import type { Station } from './Station';

export interface SegmentOverride {
    stationIndex: number; // Index in selectedStations array
    distance: number; // Custom distance from previous station in km
    duration: number; // Custom duration from previous station in minutes
}

export const VehicleClass = {
    STANDARD: 'STANDARD',
    COACH: 'COACH',
    MINI_BUS: 'MINI_BUS',
    DOUBLE_DECKER: 'DOUBLE_DECKER',
} as const;

export type VehicleClass = typeof VehicleClass[keyof typeof VehicleClass];

// Matches the backend RouteStop entity
export interface RouteStop {
    id?: number;
    station: Station;
    sequenceOrder: number;
    distanceFromPrevious: number; // km from previous stop
    durationMinutesFromPrevious: number; // minutes from previous stop
    cumulativeDistance: number; // km from start
    cumulativeDurationMinutes: number; // minutes from start
}

// Matches the backend Route entity
export interface Route {
    id: number;
    vehicleClass: VehicleClass;
    vehicleCapacity: number;
    distance: number; // in km (total)
    durationMinutes: number; // in minutes (total)
    description?: string; // optional, max length defined in calculationConstants
    routeStops: RouteStop[]; // Ordered list of stops
    // Helper methods (computed from routeStops)
    originStation?: Station;
    destinationStation?: Station;
}

// Matches the backend RouteStopRequest DTO
export interface RouteStopRequest {
    stationId: number;
    sequenceOrder: number;
    distanceFromPrevious: number; // km from previous stop
    durationMinutesFromPrevious: number; // minutes from previous stop
}

// Matches the backend CreateRouteRequest DTO
// Validation limits are defined in calculationConstants
export interface CreateRouteRequest {
    vehicleClass: VehicleClass;
    distance: number; // in km, limits: MIN_ROUTE_DISTANCE_KM to MAX_ROUTE_DISTANCE_KM (total)
    durationMinutes: number; // in minutes, limits: MIN_ROUTE_DURATION_MINUTES to MAX_ROUTE_DURATION_MINUTES (total)
    description?: string; // optional, max length: MAX_ROUTE_DESCRIPTION_LENGTH
    stops: RouteStopRequest[]; // Ordered list of stops (min: MIN_ROUTE_STOPS)
}

// Matches the backend UpdateRouteRequest DTO
// Validation limits are defined in calculationConstants
export interface UpdateRouteRequest {
    vehicleClass: VehicleClass;
    distance: number; // in km, limits: MIN_ROUTE_DISTANCE_KM to MAX_ROUTE_DISTANCE_KM (total)
    durationMinutes: number; // in minutes, limits: MIN_ROUTE_DURATION_MINUTES to MAX_ROUTE_DURATION_MINUTES (total)
    description?: string; // optional, max length: MAX_ROUTE_DESCRIPTION_LENGTH
    stops: RouteStopRequest[]; // Ordered list of stops (min: MIN_ROUTE_STOPS)
}

