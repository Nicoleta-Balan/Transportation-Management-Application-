import { useEffect, useState } from 'react';

import type { Station } from '../../../types/Station';

import { MAP_CONFIG } from '../../../constants/stationConstants';

import { BaseMap } from '../../common/BaseMap';
import { RouteMarkers } from './RouteMarkers';

interface RouteMapProps {
    stations: Station[];
    selectedStations: Station[];
    onStationSelect?: (station: Station) => void;
    height?: string;
}

export default function RouteMap({
    stations,
    selectedStations,
    onStationSelect,
    height = MAP_CONFIG.MAP_HEIGHT,
}: RouteMapProps) {
    const [center, setCenter] = useState<[number, number] | undefined>(undefined);

    // Center map on selected stations
    useEffect(() => {
        if (selectedStations.length > 0) {
            const lastSelected = selectedStations[selectedStations.length - 1];
            if (lastSelected.latitude && lastSelected.longitude) {
                setCenter([lastSelected.latitude, lastSelected.longitude]);
            }
        }
    }, [selectedStations]);

    return (
        <BaseMap
            height={height}
            center={center}
            useGeolocation={selectedStations.length === 0}
        >
            <RouteMarkers
                stations={stations}
                selectedStations={selectedStations}
                onStationSelect={onStationSelect}
            />
        </BaseMap>
    );
}

