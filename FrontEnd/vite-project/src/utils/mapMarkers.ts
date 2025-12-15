import L from 'leaflet';
import icon from 'leaflet/dist/images/marker-icon.png';
import iconShadow from 'leaflet/dist/images/marker-shadow.png';
import { UI_CONSTANTS } from '../constants/stationConstants';
import { StationStatus } from '../types/Station';

export function createDefaultMarkerIcon(): L.Icon {
    return L.icon({
        iconUrl: icon,
        shadowUrl: iconShadow,
        iconSize: [25, 41],
        iconAnchor: [12, 41],
        popupAnchor: [1, -34],
        shadowSize: [41, 41],
    });
}

function createEditingMarkerHTML(): string {
    return `<div style="
        width: 30px;
        height: 30px;
        background-color: ${UI_CONSTANTS.COLORS.ERROR};
        border: 3px solid #fff;
        border-radius: 50%;
        box-shadow: 0 2px 6px rgba(0,0,0,0.4);
        display: flex;
        align-items: center;
        justify-content: center;
    ">
        <div style="
            width: 12px;
            height: 12px;
            background-color: #fff;
            border-radius: 50%;
        "></div>
    </div>`;
}

export function createEditingMarkerIcon(): L.DivIcon {
    return L.divIcon({
        className: 'red-marker-icon',
        html: createEditingMarkerHTML(),
        iconSize: [30, 30],
        iconAnchor: [15, 15],
        popupAnchor: [0, -15],
    });
}

function createGreenMarkerHTML(): string {
    return `<div style="
        width: 30px;
        height: 30px;
        background-color: ${UI_CONSTANTS.COLORS.SUCCESS};
        border: 3px solid #fff;
        border-radius: 50%;
        box-shadow: 0 2px 6px rgba(0,0,0,0.4);
        display: flex;
        align-items: center;
        justify-content: center;
    ">
        <div style="
            width: 12px;
            height: 12px;
            background-color: #fff;
            border-radius: 50%;
        "></div>
    </div>`;
}

export function createGreenMarkerIcon(): L.DivIcon {
    return L.divIcon({
        className: 'green-marker-icon',
        html: createGreenMarkerHTML(),
        iconSize: [30, 30],
        iconAnchor: [15, 15],
        popupAnchor: [0, -15],
    });
}

function createTearShapedMarkerHTML(color: string): string {
    return `
        <div style="position: relative; width: 25px; height: 41px; margin-left: 8px;">
            <!-- Shadow (positioned behind the pin) -->
            <svg width="41" height="41" viewBox="0 0 41 41" style="position: absolute; left: -8px; top: 0; z-index: 0; pointer-events: none;">
                <ellipse cx="20.5" cy="30" rx="18" ry="8" fill="rgba(0,0,0,0.25)"/>
            </svg>
            <!-- Marker pin with exact status color from stationConstants -->
            <svg width="25" height="41" viewBox="0 0 25 41" style="position: relative; z-index: 1;">
                <path d="M12.5 0C5.596 0 0 5.596 0 12.5c0 8.75 12.5 28.5 12.5 28.5S25 21.25 25 12.5C25 5.596 19.404 0 12.5 0z" fill="${color}" stroke="#fff" stroke-width="1.5"/>
            </svg>
        </div>
    `;
}

export function createStatusMarkerIcon(status: StationStatus): L.DivIcon {
    // Get exact color from stationConstants
    let color: string;
    
    switch (status) {
        case StationStatus.ACTIVE:
            color = UI_CONSTANTS.COLORS.STATUS_ACTIVE;
            break;
        case StationStatus.INACTIVE:
            color = UI_CONSTANTS.COLORS.STATUS_INACTIVE;
            break;
        case StationStatus.MAINTENANCE:
            color = UI_CONSTANTS.COLORS.STATUS_MAINTENANCE;
            break;
        default:
            color = UI_CONSTANTS.COLORS.STATUS_INACTIVE;
    }
    
    return L.divIcon({
        className: `status-marker-icon status-${status.toLowerCase()}`,
        html: createTearShapedMarkerHTML(color),
        iconSize: [33, 41], // 25px pin + 8px left margin for shadow
        iconAnchor: [20, 41], // 12px (center of 25px pin) + 8px margin
        popupAnchor: [1, -34],
    });
}

