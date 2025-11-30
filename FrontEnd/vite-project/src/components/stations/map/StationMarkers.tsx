import { useCallback } from 'react';

import { Marker, Popup } from 'react-leaflet';

import type { Station } from '../../../types/Station';

import { createDefaultMarkerIcon, createEditingMarkerIcon, createGreenMarkerIcon } from '../../../utils/mapMarkers';
import { reverseGeocode } from '../../../utils/geocoding';

import { UI_CONSTANTS } from '../../../constants/stationConstants';

// Default icon instance (icon is already set globally in main.tsx)
const DefaultIcon = createDefaultMarkerIcon();

interface StationMarkersProps {
    stations: Station[];
    editingStationId?: number | null;
    selectedLocation?: { lat: number; lng: number } | null; // For edit mode (red marker)
    onLocationSelect: (location: { lat: number; lng: number; address: string }) => void; // For edit mode (red marker)
    isCreateFormExpanded?: boolean;
    onCreateFormLocationSelect?: (location: { lat: number; lng: number; address: string }) => void;
    createFormSelectedLocation?: { lat: number; lng: number } | null; // For create form (green marker)
}

export function StationMarkers({
    stations,
    editingStationId,
    selectedLocation, // For red marker (edit mode)
    onLocationSelect, // For red marker (edit mode)
    isCreateFormExpanded = false,
    onCreateFormLocationSelect,
    createFormSelectedLocation, // For green marker (create form)
}: StationMarkersProps) {

    const createMarkerDragHandler = useCallback((
        handler: (location: { lat: number; lng: number; address: string }) => void
    ) => {
        return async (e: L.DragEndEvent) => {
            const marker = e.target;
            const position = marker.getLatLng();
            const address = await reverseGeocode(position.lat, position.lng);
            handler({ lat: position.lat, lng: position.lng, address });
        };
    }, []);

    const handleMarkerDrag = useCallback(
        createMarkerDragHandler(onLocationSelect),
        [createMarkerDragHandler, onLocationSelect]
    );

    const handleGreenMarkerDrag = useCallback(
        (e: L.DragEndEvent) => {
            if (!onCreateFormLocationSelect) return;
            createMarkerDragHandler(onCreateFormLocationSelect)(e);
        },
        [createMarkerDragHandler, onCreateFormLocationSelect]
    );

    return (
        <>
            {/* Show green marker for create form (when create form is expanded, can be shown even in edit mode) */}
            {createFormSelectedLocation && isCreateFormExpanded && handleGreenMarkerDrag && (
                <Marker
                    position={[createFormSelectedLocation.lat, createFormSelectedLocation.lng]}
                    icon={createGreenMarkerIcon()}
                    draggable={true}
                    eventHandlers={{
                        dragend: handleGreenMarkerDrag,
                    }}
                >
                    <Popup>Selected Location for New Station (Drag to move)</Popup>
                </Marker>
            )}

            {/* Show existing stations */}
            {stations
                .filter((station) => station.latitude && station.longitude)
                .map((station) => {
                    // Use red marker for editing station, default icon for others
                    const isEditing = editingStationId === station.id;
                    const markerIcon = isEditing ? createEditingMarkerIcon() : DefaultIcon;

                    // Use selectedLocation if this is the editing station and a new location has been selected
                    const markerPosition = isEditing && selectedLocation
                        ? [selectedLocation.lat, selectedLocation.lng]
                        : [station.latitude!, station.longitude!];

                    return (
                        <Marker
                            key={station.id}
                            position={markerPosition as [number, number]}
                            icon={markerIcon}
                            draggable={isEditing}
                            eventHandlers={{
                                dragend: isEditing ? handleMarkerDrag : undefined,
                            }}
                        >
                            <Popup>
                                <strong>{station.name}</strong>
                                {isEditing && <span style={{ color: UI_CONSTANTS.COLORS.ERROR, fontWeight: 'bold' }}> (Editing - Drag to move)</span>}
                                <br />
                                {station.description && <>{station.description}<br /></>}
                                {station.address && <>{station.address}</>}
                            </Popup>
                        </Marker>
                    );
                })}
        </>
    );
}

