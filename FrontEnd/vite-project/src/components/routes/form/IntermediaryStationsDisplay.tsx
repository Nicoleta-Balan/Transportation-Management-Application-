import { useState } from 'react';
import type { Station } from '../../../types/Station';
import { calculateDistance, calculateDuration } from '../../../utils/distanceCalculator';
import {
    MIN_SEGMENT_DISTANCE,
    MAX_ROUTE_DISTANCE_KM,
    MIN_ROUTE_DURATION_MINUTES,
    MAX_ROUTE_DURATION_MINUTES,
    DISTANCE_INPUT_STEP,
    DISTANCE_DECIMAL_PLACES
} from '../../../constants/calculationConstants';

import './IntermediaryStationsDisplay.css';

interface IntermediaryStationsDisplayProps {
    selectedStations: Station[];
    segmentOverrides?: Map<number, { distance: number; duration: number }>;
    onSegmentChange?: (stationIndex: number, distance: number, duration: number) => void;
    editable?: boolean;
}

interface StationSegment {
    station: Station;
    stationIndex: number; // Index in selectedStations array
    distance: number; // Distance from previous station in km
    durationMinutes: number; // Duration from previous station in minutes
}

export function IntermediaryStationsDisplay({ 
    selectedStations, 
    segmentOverrides = new Map(),
    onSegmentChange,
    editable = false 
}: IntermediaryStationsDisplayProps) {
    const [editingIndex, setEditingIndex] = useState<number | null>(null);
    
    // Only show if more than 2 stations are selected
    if (selectedStations.length <= 2) {
        return null;
    }

    // Calculate segments for intermediary stations (excluding first and last)
    // Store both the actual (possibly overridden) values and auto-calculated values
    interface SegmentWithCalculated extends StationSegment {
        autoCalculatedDistance: number;
        autoCalculatedDuration: number;
    }
    
    const segments: SegmentWithCalculated[] = [];
    for (let i = 1; i < selectedStations.length - 1; i++) {
        const currentStation = selectedStations[i];
        const previousStation = selectedStations[i - 1];

        if (!currentStation.latitude || !currentStation.longitude || 
            !previousStation.latitude || !previousStation.longitude) {
            continue;
        }

        // Always calculate the auto-calculated values from coordinates
        const autoDistance = calculateDistance(
            previousStation.latitude,
            previousStation.longitude,
            currentStation.latitude,
            currentStation.longitude
        );
        const autoDurationHours = calculateDuration(autoDistance);
        const autoDuration = Math.round(autoDurationHours * 60);

        // Check if there's a manual override
        const override = segmentOverrides.get(i);
        
        let segmentDistance: number;
        let segmentDurationMinutes: number;
        
        if (override) {
            // Use saved/manually set values
            segmentDistance = override.distance;
            segmentDurationMinutes = override.duration;
        } else {
            // Use auto-calculated values
            segmentDistance = autoDistance;
            segmentDurationMinutes = autoDuration;
        }

        segments.push({
            station: currentStation,
            stationIndex: i,
            distance: segmentDistance,
            durationMinutes: segmentDurationMinutes,
            autoCalculatedDistance: autoDistance,
            autoCalculatedDuration: autoDuration,
        });
    }

    if (segments.length === 0) {
        return null;
    }
    
    const handleEditToggle = (index: number | null) => {
        setEditingIndex(index);
    };
    
    const handleSegmentUpdate = (stationIndex: number, distance: number, duration: number) => {
        if (onSegmentChange) {
            onSegmentChange(stationIndex, distance, duration);
        }
        setEditingIndex(null);
    };

    // Helper function to format duration as "Xh Y min"
    const formatDuration = (minutes: number): string => {
        const hours = Math.floor(minutes / 60);
        const mins = minutes % 60;
        if (hours === 0) {
            return `0h ${mins} min`;
        }
        if (mins === 0) {
            return `${hours}h`;
        }
        return `${hours}h ${mins} min`;
    };

    return (
        <div className="intermediary-stations-display">
            <h3>Intermediary Stations</h3>
            <div className="intermediary-stations-list">
                {segments.map((segment, listIndex) => (
                    <IntermediaryStationItem
                        key={segment.station.id}
                        segment={segment}
                        isEditing={editable && editingIndex === segment.stationIndex}
                        editable={editable}
                        formatDuration={formatDuration}
                        onEditToggle={handleEditToggle}
                        onSegmentUpdate={handleSegmentUpdate}
                        showDivider={listIndex < segments.length - 1}
                    />
                ))}
            </div>
        </div>
    );
}

// Separate component for each station item to properly manage state
interface IntermediaryStationItemProps {
    segment: {
        station: Station;
        stationIndex: number;
        distance: number;
        durationMinutes: number;
        autoCalculatedDistance: number;
        autoCalculatedDuration: number;
    };
    isEditing: boolean;
    editable: boolean;
    formatDuration: (minutes: number) => string;
    onEditToggle: (index: number | null) => void;
    onSegmentUpdate: (stationIndex: number, distance: number, duration: number) => void;
    showDivider: boolean;
}

function IntermediaryStationItem({
    segment,
    isEditing,
    editable,
    formatDuration,
    onEditToggle,
    onSegmentUpdate,
    showDivider,
}: IntermediaryStationItemProps) {
    // Initialize with the saved value (rounded to 1 decimal place)
    const [tempDistance, setTempDistance] = useState(Math.round(segment.distance * 10) / 10);
    const [tempDuration, setTempDuration] = useState(segment.durationMinutes);
    
    // Use the true auto-calculated values for help text
    const autoCalculatedDistance = Math.round(segment.autoCalculatedDistance * 10) / 10;
    const autoCalculatedDuration = segment.autoCalculatedDuration;
    
    return (
        <div className="intermediary-station-item">
            <div className="station-info">
                <strong>{segment.station.name}</strong>
                {segment.station.address && (
                    <span className="station-address"> - {segment.station.address}</span>
                )}
            </div>
            {isEditing ? (
                <div className="segment-metrics-edit">
                    <label>
                        Distance from Previous (km):
                        <input
                            type="number"
                            step={DISTANCE_INPUT_STEP}
                            min={MIN_SEGMENT_DISTANCE}
                            max={MAX_ROUTE_DISTANCE_KM}
                            value={tempDistance}
                            onChange={(e) => {
                                const value = parseFloat(e.target.value) || 0;
                                // Round to 1 decimal place
                                const rounded = Math.round(value * 10) / 10;
                                setTempDistance(rounded);
                            }}
                            className="segment-input"
                        />
                        <span className="help-text">Auto-calculated: {autoCalculatedDistance.toFixed(DISTANCE_DECIMAL_PLACES)} km</span>
                    </label>
                    <label>
                        Duration from Previous (min):
                        <input
                            type="number"
                            min={MIN_ROUTE_DURATION_MINUTES}
                            max={MAX_ROUTE_DURATION_MINUTES}
                            value={tempDuration}
                            onChange={(e) => setTempDuration(parseInt(e.target.value) || 0)}
                            className="segment-input"
                        />
                        <span className="help-text">Auto-calculated: {formatDuration(autoCalculatedDuration)}</span>
                    </label>
                    <div className="segment-actions">
                        <button
                            type="button"
                            onClick={() => {
                                // Round distance to 1 decimal place before saving
                                const roundedDistance = Math.round(tempDistance * 10) / 10;
                                onSegmentUpdate(segment.stationIndex, roundedDistance, tempDuration);
                            }}
                            className="btn-save-segment"
                        >
                            Save
                        </button>
                        <button
                            type="button"
                            onClick={() => {
                                setTempDistance(Math.round(segment.distance * 10) / 10);
                                setTempDuration(segment.durationMinutes);
                                onEditToggle(null);
                            }}
                            className="btn-cancel-segment"
                        >
                            Cancel
                        </button>
                    </div>
                </div>
            ) : (
                        <div className="segment-metrics">
                            <span className="segment-distance">
                                Distance from Previous: {segment.distance.toFixed(DISTANCE_DECIMAL_PLACES)} km
                            </span>
                    <span className="segment-duration">
                        Duration from Previous: {formatDuration(segment.durationMinutes)}
                    </span>
                    {editable && (
                        <button
                            type="button"
                            onClick={() => {
                                setTempDistance(Math.round(segment.distance * 10) / 10);
                                setTempDuration(segment.durationMinutes);
                                onEditToggle(segment.stationIndex);
                            }}
                            className="btn-edit-segment"
                        >
                            Edit
                        </button>
                    )}
                </div>
            )}
            {showDivider && <div className="segment-divider" />}
        </div>
    );
}

