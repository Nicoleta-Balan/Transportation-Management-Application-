import { useCallback } from 'react';

import { Marker, Popup, Polyline } from 'react-leaflet';

import type { Station } from '../../../types/Station';

import { createStatusMarkerIcon, createGreenMarkerIcon } from '../../../utils/mapMarkers';

interface RouteMarkersProps {
    stations: Station[]; // All stations (will be filtered to ACTIVE only)
    selectedStations: Station[]; // Stations selected for the route
    onStationSelect?: (station: Station) => void;
}

export function RouteMarkers({
    stations,
    selectedStations,
    onStationSelect,
}: RouteMarkersProps) {
    // Filter to only ACTIVE stations
    const activeStations = stations.filter(station => station.status === 'ACTIVE');

    // Handle marker click
    const handleMarkerClick = useCallback((station: Station) => {
        onStationSelect?.(station);
    }, [onStationSelect]);

    // Get selected station IDs for quick lookup
    const selectedStationIds = new Set(selectedStations.map(s => s.id));

    // Create polyline positions for route visualization
    const routePositions = selectedStations
        .filter(station => station.latitude && station.longitude)
        .map(station => [station.latitude!, station.longitude!] as [number, number]);

    return (
        <>
            {/* Draw route line connecting selected stations */}
            {routePositions.length >= 2 && (
                <Polyline
                    positions={routePositions}
                    color="#3388ff"
                    weight={3}
                    opacity={0.7}
                />
            )}

            {/* Show active stations */}
            {activeStations
                .filter((station) => station.latitude && station.longitude)
                .map((station) => {
                    const isSelected = selectedStationIds.has(station.id);
                    // Use green marker for selected stations, status marker for others
                    const markerIcon = isSelected 
                        ? createGreenMarkerIcon() 
                        : createStatusMarkerIcon(station.status);

                    return (
                        <Marker
                            key={station.id}
                            position={[station.latitude!, station.longitude!]}
                            icon={markerIcon}
                            eventHandlers={{
                                click: () => handleMarkerClick(station),
                            }}
                        >
                            <Popup>
                                <strong>{station.name}</strong>
                                {isSelected && <span style={{ color: '#28a745', fontWeight: 'bold' }}> (Selected)</span>}
                                <br />
                                {station.description && <>{station.description}<br /></>}
                                {station.address && <>{station.address}</>}
                                <br />
                                <small style={{ color: '#666' }}>Click to select for route</small>
                            </Popup>
                        </Marker>
                    );
                })}
        </>
    );
}

