/**
 * Reverse geocodes coordinates to get an address
 * Uses Nominatim (OpenStreetMap) reverse geocoding service
 * 
 * @param lat - Latitude coordinate
 * @param lng - Longitude coordinate
 * @returns Promise resolving to the address string
 */
export async function reverseGeocode(lat: number, lng: number): Promise<string> {
    try {
        const response = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`);
        const data = await response.json();
        return data.display_name || `${lat.toFixed(6)}, ${lng.toFixed(6)}`;
    } catch {
        return `${lat.toFixed(6)}, ${lng.toFixed(6)}`;
    }
}

