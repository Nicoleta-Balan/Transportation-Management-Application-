/**
 * Constants for distance and duration calculations
 */

/** Earth's radius in kilometers (used in Haversine formula) */
export const EARTH_RADIUS_KM = 6371;

/** Default average speed for duration calculations in km/h */
export const DEFAULT_SPEED_KMH = 70;

/** Conversion factor from degrees to radians */
export const DEGREES_TO_RADIANS = Math.PI / 180;

/**
 * Route validation constants
 */

/** Maximum distance for a route in kilometers */
export const MAX_ROUTE_DISTANCE_KM = 999;

/** Minimum distance for a route in kilometers */
export const MIN_ROUTE_DISTANCE_KM = 1;

/** Maximum duration for a route in minutes (48 hours) */
export const MAX_ROUTE_DURATION_MINUTES = 2880;

/** Maximum duration for a route in hours */
export const MAX_ROUTE_DURATION_HOURS = 48;

/** Minimum duration for a route in minutes */
export const MIN_ROUTE_DURATION_MINUTES = 1;

/** Maximum length for route description */
export const MAX_ROUTE_DESCRIPTION_LENGTH = 50;

/** Number of decimal places for distance display */
export const DISTANCE_DECIMAL_PLACES = 1;

/** Multiplier for rounding distance to specified decimal places */
export const DISTANCE_DECIMAL_PLACES_MULTIPLIER = Math.pow(10, DISTANCE_DECIMAL_PLACES);

/** Minimum number of stops required for a route */
export const MIN_ROUTE_STOPS = 2;

/** Maximum value for segment distance input step */
export const DISTANCE_INPUT_STEP = 0.1;

/** Minimum value for segment distance */
export const MIN_SEGMENT_DISTANCE = 0.1;

