import { useEffect, useRef, useState, useCallback, type ReactNode } from 'react';

import { MapContainer, TileLayer } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

import { MAP_CONFIG, UI_CONSTANTS } from '../../constants/stationConstants';

interface BaseMapProps {
    /** Child components to render inside the map (markers, polylines, etc.) */
    children: ReactNode;
    /** Map height (defaults to MAP_CONFIG.MAP_HEIGHT) */
    height?: string;
    /** Initial center coordinates (defaults to MAP_CONFIG.DEFAULT_CENTER) */
    initialCenter?: [number, number];
    /** Center coordinates to update to (triggers map view update) */
    center?: [number, number];
    /** Initial zoom level (defaults to MAP_CONFIG.DEFAULT_ZOOM) */
    zoom?: number;
    /** Callback when center changes */
    onCenterChange?: (center: [number, number]) => void;
    /** Whether to try to get user's location on mount */
    useGeolocation?: boolean;
}

/* - Base map component that provides common map functionality:
   - Map initialization and sizing
   - Geolocation support
   - Window resize handling
   - Center management */
export function BaseMap({
    children,
    height = MAP_CONFIG.MAP_HEIGHT,
    initialCenter = MAP_CONFIG.DEFAULT_CENTER,
    center,
    zoom = MAP_CONFIG.DEFAULT_ZOOM,
    onCenterChange,
    useGeolocation = true,
}: BaseMapProps) {
    const [mapCenter, setMapCenter] = useState<[number, number]>(initialCenter);
    const mapRef = useRef<L.Map | null>(null);

    const invalidateMapSize = useCallback(() => {
        setTimeout(() => {
            mapRef.current?.invalidateSize();
        }, UI_CONSTANTS.MAP_INVALIDATE_DELAY);
    }, []);

    const updateMapCenter = useCallback((newCenter: [number, number]) => {
        setMapCenter(newCenter);
        mapRef.current?.setView(newCenter, zoom);
        invalidateMapSize();
        onCenterChange?.(newCenter);
    }, [zoom, invalidateMapSize, onCenterChange]);

    // Update center when center prop changes
    useEffect(() => {
        if (center) {
            updateMapCenter(center);
        }
    }, [center, updateMapCenter]);

    // Try to get user's location on mount
    useEffect(() => {
        if (useGeolocation && navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                (position) => {
                    const { latitude, longitude } = position.coords;
                    updateMapCenter([latitude, longitude]);
                },
                () => {
                    // If geolocation fails, silently fall back to default center
                }
            );
        }
    }, [useGeolocation, updateMapCenter]);

    // Fix map size on mount and window resize
    useEffect(() => {
        const timer = setTimeout(() => {
            mapRef.current?.invalidateSize();
        }, UI_CONSTANTS.MAP_INVALIDATE_DELAY);

        const handleResize = () => {
            mapRef.current?.invalidateSize();
        };
        window.addEventListener('resize', handleResize);

        return () => {
            clearTimeout(timer);
            window.removeEventListener('resize', handleResize);
        };
    }, []);

    return (
        <div style={{ height, width: '100%', borderRadius: '8px', overflow: 'hidden' }}>
            <MapContainer
                center={mapCenter}
                zoom={zoom}
                style={{ height: '100%', width: '100%' }}
                ref={mapRef}
            >
                <TileLayer
                    attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                />
                {children}
            </MapContainer>
        </div>
    );
}

