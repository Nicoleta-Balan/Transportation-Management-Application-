import { useEffect, useState } from 'react';

import type { Station } from '../../../types/Station';

import { MAP_CONFIG } from '../../../constants/stationConstants';

import { BaseMap } from '../../common/BaseMap';
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
    const [center, setCenter] = useState<[number, number] | undefined>(
        initialLocation ? [initialLocation.lat, initialLocation.lng] : undefined
    );

    // Update center when location is selected
    useEffect(() => {
        if (selectedLocation) {
            setCenter([selectedLocation.lat, selectedLocation.lng]);
        }
    }, [selectedLocation]);

    return (
        <BaseMap
            height={height}
            initialCenter={initialLocation ? [initialLocation.lat, initialLocation.lng] : undefined}
            center={center}
            useGeolocation={!initialLocation && !selectedLocation}
        >
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
        </BaseMap>
    );
}

