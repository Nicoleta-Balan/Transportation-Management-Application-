import { createContext, useContext, type ReactNode } from 'react';

import type { UseFormRegister, UseFormHandleSubmit, UseFormWatch, FieldErrors } from 'react-hook-form';

import { useEditStationForm } from '../hooks/useEditStationForm';

import type { Station, UpdateStationRequest } from '../types/Station';

interface StationEditFormContextValue {
    // Edit form state
    editingStation: Station | null;
    editSelectedLocation: { lat: number; lng: number } | null;
    editError: string | null;
    updating: boolean;
    
    // Edit form methods
    registerEdit: UseFormRegister<UpdateStationRequest>;
    handleEditSubmitForm: UseFormHandleSubmit<UpdateStationRequest>;
    watchEdit: UseFormWatch<UpdateStationRequest>;
    editErrors: FieldErrors<UpdateStationRequest>;
    isEditValid: boolean;
    
    // Edit form handlers
    handleEditClick: (station: Station) => void;
    handleEditSubmit: (data: UpdateStationRequest) => void;
    handleCancelEdit: () => void;
    handleEditMapLocationSelect: (location: { lat: number; lng: number; address: string }) => void;
}

const StationEditFormContext = createContext<StationEditFormContextValue | null>(null);

interface StationEditFormProviderProps {
    children: ReactNode;
    loadStations: () => Promise<void>;
}

export function StationEditFormProvider({ children, loadStations }: StationEditFormProviderProps) {
    const editForm = useEditStationForm(loadStations);

    return (
        <StationEditFormContext.Provider value={editForm}>
            {children}
        </StationEditFormContext.Provider>
    );
}

export function useStationEditFormContext(): StationEditFormContextValue {
    const context = useContext(StationEditFormContext);
    if (!context) {
        throw new Error('useStationEditFormContext must be used within StationEditFormProvider');
    }
    return context;
}

