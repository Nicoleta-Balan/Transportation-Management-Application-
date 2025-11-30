export const VALIDATION_RULES = {
    NAME_MIN_LENGTH: 2,
    NAME_MAX_LENGTH: 100,
    DESCRIPTION_MAX_LENGTH: 255,
    ADDRESS_MAX_LENGTH: 500,
    LATITUDE_MIN: -90,
    LATITUDE_MAX: 90,
    LONGITUDE_MIN: -180,
    LONGITUDE_MAX: 180,
} as const;

export const MAP_CONFIG = {
    DEFAULT_CENTER: [47.1585, 27.6014] as [number, number], // Iasi, Romania coordinates
    DEFAULT_ZOOM: 13,
    MAP_HEIGHT: '400px',
} as const;

export const MAP_INSTRUCTIONS = {
    edit: 'Drag the red marker to change the station location',
    create: 'Click on the map to select location',
} as const;

export const UI_CONSTANTS = {
    MAP_INVALIDATE_DELAY: 100,
    COLORS: {
        ERROR: '#dc3545',      // Red - errors, delete buttons, editing markers
        SUCCESS: '#28a745',    // Green - success states, create markers
    },
} as const;

