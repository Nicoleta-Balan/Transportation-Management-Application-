import { VehicleClass } from '../types/Route';

export const VEHICLE_CLASS_OPTIONS: Array<{ value: string; label: string }> = [
    { value: VehicleClass.STANDARD, label: 'Standard (50 seats)' },
    { value: VehicleClass.COACH, label: 'Coach (60 seats)' },
    { value: VehicleClass.MINI_BUS, label: 'Mini Bus (20 seats)' },
    { value: VehicleClass.DOUBLE_DECKER, label: 'Double Decker (80 seats)' },
];

