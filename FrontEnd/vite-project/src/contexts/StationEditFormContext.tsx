import type { UseFormRegister, UseFormHandleSubmit, UseFormWatch, FieldErrors } from 'react-hook-form';

import { useEditStationForm } from '../hooks/useEditStationForm';
import { createEditFormContext } from '../utils/createEditFormContext';

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

const { Provider: BaseProvider, useContextHook } = createEditFormContext<StationEditFormContextValue>('StationEditForm');

interface StationEditFormProviderProps {
    children: React.ReactNode;
    loadStations: () => Promise<void>;
}

export function StationEditFormProvider({ children, loadStations }: StationEditFormProviderProps) {
    const editForm = useEditStationForm(loadStations);

    return (
        <BaseProvider value={editForm}>
            {children}
        </BaseProvider>
    );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useStationEditFormContext(): StationEditFormContextValue {
    return useContextHook();
}

