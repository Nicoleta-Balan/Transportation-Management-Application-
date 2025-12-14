import type { Station } from './Station';
import type { Route } from './Route';

export interface TimetableStop {
    id?: number;
    station: Station;
    sequenceOrder: number;
    arrivalTime: string; // ISO 8601 datetime string
    departureTime?: string | null; // ISO 8601 datetime string, null for end station
}

export interface Timetable {
    id: number;
    route: Route;
    description?: string;
    startDate?: string; // ISO date string (YYYY-MM-DD)
    endDate?: string; // ISO date string (YYYY-MM-DD)
    daysOfWeek?: string[]; // Array of day names (e.g., ["Mon", "Tue", "Wed"])
    timetableStops: TimetableStop[];
}

export interface TimetableStopRequest {
    stationId: number;
    sequenceOrder: number;
    arrivalTime: string; // ISO 8601 datetime string
    departureTime?: string | null; // ISO 8601 datetime string, null for end station
}

export interface CreateTimetableRequest {
    routeId: number;
    description?: string;
    startDate?: string; // ISO date string (YYYY-MM-DD)
    endDate?: string; // ISO date string (YYYY-MM-DD)
    daysOfWeek?: string[]; // Array of day names (e.g., ["Mon", "Tue", "Wed"])
    stops: TimetableStopRequest[];
}

export interface UpdateTimetableRequest {
    description?: string;
    startDate?: string; // ISO date string (YYYY-MM-DD)
    endDate?: string; // ISO date string (YYYY-MM-DD)
    daysOfWeek?: string[]; // Array of day names (e.g., ["Mon", "Tue", "Wed"])
    stops: TimetableStopRequest[];
}

