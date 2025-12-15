import { VehicleClass } from '../types/Route';
import type { RegisterOptions } from 'react-hook-form';
import type { CreateRouteRequest, UpdateRouteRequest } from '../types/Route';
import {
    MIN_ROUTE_DISTANCE_KM,
    MAX_ROUTE_DISTANCE_KM,
    MIN_ROUTE_DURATION_MINUTES,
    MAX_ROUTE_DURATION_MINUTES,
    MAX_ROUTE_DURATION_HOURS,
    MAX_ROUTE_DESCRIPTION_LENGTH
} from '../constants/calculationConstants';

// Helper function to validate vehicle class
const validateVehicleClass = (value: string | undefined): string | true => {
    if (!value) {
        return 'Vehicle class is required';
    }
    const validVehicleClasses: VehicleClass[] = [
        VehicleClass.STANDARD,
        VehicleClass.COACH,
        VehicleClass.MINI_BUS,
        VehicleClass.DOUBLE_DECKER,
    ];
    return validVehicleClasses.includes(value as VehicleClass) || 'Invalid vehicle class selected';
};

// Station validation is now handled via stops array (selectedStations.length >= 2)
// No longer needed as originStationId and destinationStationId are removed

export const vehicleClassValidation: RegisterOptions<CreateRouteRequest | UpdateRouteRequest, 'vehicleClass'> = {
    required: 'Vehicle class is required',
    validate: validateVehicleClass,
};

// Helper function to validate distance
const validateDistance = (value: number | undefined | null): string | true => {
    if (value === undefined || value === null) {
        return 'Distance is required';
    }
    if (typeof value !== 'number' || isNaN(value)) {
        return 'Distance must be a valid number';
    }
    if (value < MIN_ROUTE_DISTANCE_KM) {
        return `Distance must be at least ${MIN_ROUTE_DISTANCE_KM} km`;
    }
    if (value > MAX_ROUTE_DISTANCE_KM) {
        return `Distance cannot exceed ${MAX_ROUTE_DISTANCE_KM} km`;
    }
    return true;
};

// Helper function to validate duration (in minutes)
const validateDurationMinutes = (value: number | undefined | null): string | true => {
    if (value === undefined || value === null) {
        return 'Duration is required';
    }
    if (typeof value !== 'number' || isNaN(value)) {
        return 'Duration must be a valid number';
    }
    if (value < MIN_ROUTE_DURATION_MINUTES) {
        return `Duration must be at least ${MIN_ROUTE_DURATION_MINUTES} minute`;
    }
    if (value > MAX_ROUTE_DURATION_MINUTES) {
        return `Duration cannot exceed ${MAX_ROUTE_DURATION_HOURS} hours (${MAX_ROUTE_DURATION_MINUTES} minutes)`;
    }
    return true;
};

export const distanceValidation: RegisterOptions<CreateRouteRequest | UpdateRouteRequest, 'distance'> = {
    required: 'Distance is required',
    valueAsNumber: true,
    min: {
        value: MIN_ROUTE_DISTANCE_KM,
        message: `Distance must be at least ${MIN_ROUTE_DISTANCE_KM} km`,
    },
    max: {
        value: MAX_ROUTE_DISTANCE_KM,
        message: `Distance cannot exceed ${MAX_ROUTE_DISTANCE_KM} km`,
    },
    validate: validateDistance,
};

export const durationMinutesValidation: RegisterOptions<CreateRouteRequest | UpdateRouteRequest, 'durationMinutes'> = {
    required: 'Duration is required',
    valueAsNumber: true,
    min: {
        value: MIN_ROUTE_DURATION_MINUTES,
        message: `Duration must be at least ${MIN_ROUTE_DURATION_MINUTES} minute`,
    },
    max: {
        value: MAX_ROUTE_DURATION_MINUTES,
        message: `Duration cannot exceed ${MAX_ROUTE_DURATION_HOURS} hours (${MAX_ROUTE_DURATION_MINUTES} minutes)`,
    },
    validate: validateDurationMinutes,
};

export const descriptionValidation: RegisterOptions<CreateRouteRequest | UpdateRouteRequest, 'description'> = {
    maxLength: {
        value: MAX_ROUTE_DESCRIPTION_LENGTH,
        message: `Description must not exceed ${MAX_ROUTE_DESCRIPTION_LENGTH} characters`,
    },
};

