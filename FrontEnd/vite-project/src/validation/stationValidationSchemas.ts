import { StationStatus } from '../types/Station';
import { VALIDATION_RULES } from '../constants/stationConstants';
import type { RegisterOptions } from 'react-hook-form';
import type { CreateStationRequest } from '../types/Station';

// Helper function to validate latitude
const validateLatitude = (value: number | undefined | null): string | true => {
    if (value === undefined || value === null) {
        return 'Please select a location on the map';
    }
    const numValue = value;
    if (isNaN(numValue)) {
        return 'Latitude must be a valid number';
    }
    if (numValue < VALIDATION_RULES.LATITUDE_MIN || numValue > VALIDATION_RULES.LATITUDE_MAX) {
        return `Latitude must be between ${VALIDATION_RULES.LATITUDE_MIN} and ${VALIDATION_RULES.LATITUDE_MAX}`;
    }
    return true;
};

// Helper function to validate longitude
const validateLongitude = (value: number | undefined | null): string | true => {
    if (value === undefined || value === null) {
        return 'Please select a location on the map';
    }
    const numValue = value;
    if (isNaN(numValue)) {
        return 'Longitude must be a valid number';
    }
    if (numValue < VALIDATION_RULES.LONGITUDE_MIN || numValue > VALIDATION_RULES.LONGITUDE_MAX) {
        return `Longitude must be between ${VALIDATION_RULES.LONGITUDE_MIN} and ${VALIDATION_RULES.LONGITUDE_MAX}`;
    }
    return true;
};

// Helper function to validate status
const validateStatus = (value: string | undefined): string | true => {
    if (!value) {
        return 'Status is required';
    }
    const validStatuses: StationStatus[] = [
        StationStatus.ACTIVE,
        StationStatus.INACTIVE,
        StationStatus.MAINTENANCE,
    ];
    return validStatuses.includes(value as StationStatus) || 'Invalid status selected';
};

export const nameValidation: RegisterOptions<CreateStationRequest, 'name'> = {
    required: 'Station name is required',
    minLength: {
        value: VALIDATION_RULES.NAME_MIN_LENGTH,
        message: `Name must be at least ${VALIDATION_RULES.NAME_MIN_LENGTH} characters`,
    },
    maxLength: {
        value: VALIDATION_RULES.NAME_MAX_LENGTH,
        message: `Name must be less than ${VALIDATION_RULES.NAME_MAX_LENGTH} characters`,
    },
};

export const descriptionValidation = {
    maxLength: {
        value: VALIDATION_RULES.DESCRIPTION_MAX_LENGTH,
        message: `Description must be less than ${VALIDATION_RULES.DESCRIPTION_MAX_LENGTH} characters`,
    },
} as const;

export const statusValidation = {
    required: 'Status is required',
    validate: validateStatus,
} as const;

export const addressValidation = {
    required: 'Address is required. Please select a location on the map.',
    maxLength: {
        value: VALIDATION_RULES.ADDRESS_MAX_LENGTH,
        message: `Address must be less than ${VALIDATION_RULES.ADDRESS_MAX_LENGTH} characters`,
    },
    minLength: {
        value: 1,
        message: 'Address cannot be empty',
    },
} as const;

export const latitudeValidation = {
    required: 'Please select a location on the map',
    valueAsNumber: true,
    validate: validateLatitude,
} as const;

export const longitudeValidation = {
    required: 'Please select a location on the map',
    valueAsNumber: true,
    validate: validateLongitude,
} as const;
