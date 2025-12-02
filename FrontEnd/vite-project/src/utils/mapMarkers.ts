import L from 'leaflet';
import icon from 'leaflet/dist/images/marker-icon.png';
import iconShadow from 'leaflet/dist/images/marker-shadow.png';
import { UI_CONSTANTS } from '../constants/stationConstants';

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

