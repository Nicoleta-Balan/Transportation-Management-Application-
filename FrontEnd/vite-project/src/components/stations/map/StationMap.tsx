import { useEffect, useRef, useState, useCallback } from 'react';

import { MapContainer, TileLayer } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

import type { Station } from '../../../types/Station';

import { MAP_CONFIG, UI_CONSTANTS } from '../../../constants/stationConstants';

import { MapClickHandler } from './MapClickHandler';
import { StationMarkers } from './StationMarkers';

interface StationMapProps {
    onLocationSelect: (location: { lat: number; lng: number; address: string }) => void;
    selectedLocation?: { lat: number; lng: number } | null; // For edit mode (red marker)
    existingStations?: Station[];
    height?: string;
    initialLocation?: { lat: number; lng: number };
    editingStationId?: number | null;
    onCreateFormLocationSelect?: (location: { lat: number; lng: number; address: string }) => void;
    isCreateFormExpanded?: boolean;
    createFormSelectedLocation?: { lat: number; lng: number } | null; // For create form (green marker)
}


export default function StationMap({
                                       onLocationSelect,
                                       selectedLocation,
                                       existingStations = [],
                                       height = MAP_CONFIG.MAP_HEIGHT,
                                       initialLocation,
                                       editingStationId,
                                       onCreateFormLocationSelect,
                                       isCreateFormExpanded = false,
                                       createFormSelectedLocation,
                                   }: StationMapProps) {
    const [center, setCenter] = useState<[number, number]>(
        initialLocation ? [initialLocation.lat, initialLocation.lng] : MAP_CONFIG.DEFAULT_CENTER
    );
    const mapRef = useRef<L.Map | null>(null);

    const invalidateMapSize = useCallback(() => {
        setTimeout(() => {
            mapRef.current?.invalidateSize();
        }, UI_CONSTANTS.MAP_INVALIDATE_DELAY);
    }, []);

    const updateMapCenter = useCallback((location: { lat: number; lng: number }) => {
        setCenter([location.lat, location.lng]);
        mapRef.current?.setView([location.lat, location.lng], MAP_CONFIG.DEFAULT_ZOOM);
        invalidateMapSize();
    }, [invalidateMapSize]);

    // Update center when initialLocation is provided (for edit mode)
    useEffect(() => {
        if (initialLocation) {
            updateMapCenter(initialLocation);
        }
    }, [initialLocation, updateMapCenter]);

    // Update center when location is selected
    useEffect(() => {
        if (selectedLocation) {
            updateMapCenter(selectedLocation);
        }
    }, [selectedLocation, updateMapCenter]);

    // Try to get user's location on mount
    useEffect(() => {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                (position) => {
                    const { latitude, longitude } = position.coords;
                    setCenter([latitude, longitude]);
                },
                () => {
                    // If geolocation fails, use default location (already set in useState)
                    // Silently fall back to default - no user action needed
                }
            );
        }
    }, []);

    // Fix map size on mount and window resize
    useEffect(() => {
        // Invalidate size when component mounts
        const timer = setTimeout(() => {
            mapRef.current?.invalidateSize();
        }, UI_CONSTANTS.MAP_INVALIDATE_DELAY);

        // Also invalidate on window resize
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
                center={center}
                zoom={MAP_CONFIG.DEFAULT_ZOOM}
                style={{ height: '100%', width: '100%' }}
                ref={mapRef}
            >
                <TileLayer
                    attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                />
                <MapClickHandler 
                    onLocationSelect={editingStationId && onCreateFormLocationSelect ? onCreateFormLocationSelect : onLocationSelect} 
                />
                <StationMarkers
                    stations={existingStations}
                    editingStationId={editingStationId}
                    selectedLocation={selectedLocation}
                    onLocationSelect={onLocationSelect}
                    isCreateFormExpanded={isCreateFormExpanded}
                    onCreateFormLocationSelect={onCreateFormLocationSelect}
                    createFormSelectedLocation={createFormSelectedLocation}
                />
            </MapContainer>
        </div>
    );
}

