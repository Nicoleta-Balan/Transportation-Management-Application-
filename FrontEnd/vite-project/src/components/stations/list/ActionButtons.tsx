import type { Station } from '../../../types/Station';

import { useStationEditFormContext } from '../../../contexts/StationEditFormContext';

import { EditButton, DeleteButton } from '../../buttons';

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
        <div className="action-buttons-container">
            <div className="action-buttons">
                <EditButton
                    onClick={() => handleEditClick(station)}
                    disabled={editingStation !== null && editingStation.id !== station.id || deleting === station.id}
                    ariaLabel={`Edit station ${station.name}`}
                />
                <DeleteButton
                    onClick={() => onDeleteClick(station)}
                    disabled={editingStation !== null || deleting === station.id}
                    isDeleting={deleting === station.id}
                    ariaLabel={`Delete station ${station.name}`}
                />
            </div>
            {deleteError[station.id] && (
                <div className="delete-error-message" role="alert">
                    {deleteError[station.id]}
                </div>
            )}
        </div>
    );
}

