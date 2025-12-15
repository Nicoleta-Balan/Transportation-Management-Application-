import type { Station } from '../../../types/Station';
import { useStationEditFormContext } from '../../../contexts/StationEditFormContext';
import { ResourceActionButtons } from '../../common/ResourceActionButtons';

interface ActionButtonsProps {
    station: Station;
    deleting: number | null;
    deleteError: Record<number, string>;
    onDeleteClick: (station: Station) => void;
}

export default function ActionButtons({
    station,
    deleting,
    deleteError,
    onDeleteClick,
}: ActionButtonsProps) {
    // Get edit form state from context
    const { editingStation, handleEditClick } = useStationEditFormContext();
    
    return (
        <ResourceActionButtons
            resource={station}
            editingId={editingStation?.id ?? null}
            deleting={deleting}
            deleteError={deleteError}
            onEditClick={handleEditClick}
            onDeleteClick={onDeleteClick}
            getEditAriaLabel={(s) => `Edit station ${s.name}`}
            getDeleteAriaLabel={(s) => `Delete station ${s.name}`}
        />
    );
}

