import type { ReactNode } from 'react';

import type { UseFormRegister, UseFormHandleSubmit, FieldErrors } from 'react-hook-form';

import { useEditRouteForm } from '../hooks/useEditRouteForm';
import { createEditFormContext } from '../utils/createEditFormContext';

import type { Route, UpdateRouteRequest } from '../types/Route';
import type { Station } from '../types/Station';

interface RouteEditFormContextValue {
    // Edit form state
    editingRoute: Route | null;
    error: string | null;
    updating: boolean;
    selectedStations: Station[];
    segmentOverrides: Map<number, { distance: number; duration: number }>;
    calculatedDistance: number;
    calculatedDurationMinutes: number;
    
    // Edit form methods
    register: UseFormRegister<UpdateRouteRequest>;
    handleSubmit: UseFormHandleSubmit<UpdateRouteRequest>;
    errors: FieldErrors<UpdateRouteRequest>;
    
    // Edit form handlers
    handleEditClick: (route: Route) => void;
    handleStationSelect: (station: Station) => void;
    handleSegmentChange: (stationIndex: number, distance: number, duration: number) => void;
    onSegmentChange: (stationIndex: number, distance: number, duration: number) => void;
    onSubmit: (data: UpdateRouteRequest) => void;
    resetForm: () => void;
    loadRoutes: () => Promise<void>;
}

const { Provider: BaseProvider, useContextHook } = createEditFormContext<RouteEditFormContextValue>('RouteEditForm');

interface RouteEditFormProviderProps {
    children: ReactNode;
    loadRoutes: () => Promise<void>;
}

export function RouteEditFormProvider({ children, loadRoutes }: RouteEditFormProviderProps) {
    const editForm = useEditRouteForm(loadRoutes);

    const contextValue: RouteEditFormContextValue = {
        editingRoute: editForm.editingRoute,
        error: editForm.error,
        updating: editForm.updating,
        selectedStations: editForm.selectedStations,
        segmentOverrides: editForm.segmentOverrides,
        calculatedDistance: editForm.calculatedDistance,
        calculatedDurationMinutes: editForm.calculatedDurationMinutes,
        register: editForm.register,
        handleSubmit: editForm.handleSubmit,
        errors: editForm.errors,
        handleEditClick: editForm.handleEditClick,
        handleStationSelect: editForm.handleStationSelect,
        handleSegmentChange: editForm.handleSegmentChange,
        onSegmentChange: editForm.handleSegmentChange,
        onSubmit: editForm.onSubmit,
        resetForm: editForm.resetForm,
        loadRoutes,
    };

    return (
        <BaseProvider value={contextValue}>
            {children}
        </BaseProvider>
    );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useRouteEditFormContext(): RouteEditFormContextValue {
    return useContextHook();
}

