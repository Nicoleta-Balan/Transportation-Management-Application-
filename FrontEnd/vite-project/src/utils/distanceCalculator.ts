import { EARTH_RADIUS_KM, DEGREES_TO_RADIANS, DEFAULT_SPEED_KMH } from '../constants/calculationConstants';

export function calculateDistance(
    lat1: number,
    lon1: number,
    lat2: number,
    lon2: number
): number {
    const dLat = toRadians(lat2 - lat1);
    const dLon = toRadians(lon2 - lon1);

    const a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(toRadians(lat1)) *
            Math.cos(toRadians(lat2)) *
            Math.sin(dLon / 2) *
            Math.sin(dLon / 2);

    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const distance = EARTH_RADIUS_KM * c;

    return distance;
}

function toRadians(degrees: number): number {
    return degrees * DEGREES_TO_RADIANS;
}

export function calculateTotalDistance(stations: Array<{ latitude: number; longitude: number }>): number {
    if (stations.length < 2) {
        return 0;
    }

    let totalDistance = 0;
    for (let i = 0; i < stations.length - 1; i++) {
        const current = stations[i];
        const next = stations[i + 1];
        totalDistance += calculateDistance(
            current.latitude,
            current.longitude,
            next.latitude,
            next.longitude
        );
    }

    return totalDistance;
}

export function calculateDuration(distance: number, speed: number = DEFAULT_SPEED_KMH): number {
    if (speed <= 0) {
        return 0;
    }
    return distance / speed;
}

