import { EditButton, DeleteButton } from '../buttons';

interface ResourceActionButtonsProps<T extends { id: number }> {
    /** The resource item */
    resource: T;
    /** ID of the resource currently being edited (null if none) */
    editingId: number | null;
    /** ID of the resource currently being deleted (null if none) */
    deleting: number | null;
    /** Delete error messages keyed by resource ID */
    deleteError: Record<number, string>;
    /** Handler for edit button click */
    onEditClick: (resource: T) => void;
    /** Handler for delete button click */
    onDeleteClick: (resource: T) => void;
    /** Function to generate aria label for edit button */
    getEditAriaLabel: (resource: T) => string;
    /** Function to generate aria label for delete button */
    getDeleteAriaLabel: (resource: T) => string;
}

export function ResourceActionButtons<T extends { id: number }>({
    resource,
    editingId,
    deleting,
    deleteError,
    onEditClick,
    onDeleteClick,
    getEditAriaLabel,
    getDeleteAriaLabel,
}: ResourceActionButtonsProps<T>) {
    return (
        <div className="action-buttons-container">
            <div className="action-buttons">
                <EditButton
                    onClick={() => onEditClick(resource)}
                    disabled={editingId !== null && editingId !== resource.id || deleting === resource.id}
                    ariaLabel={getEditAriaLabel(resource)}
                />
                <DeleteButton
                    onClick={() => onDeleteClick(resource)}
                    disabled={editingId !== null || deleting === resource.id}
                    isDeleting={deleting === resource.id}
                    ariaLabel={getDeleteAriaLabel(resource)}
                />
            </div>
            {deleteError[resource.id] && (
                <div className="delete-error-message" role="alert">
                    {deleteError[resource.id]}
                </div>
            )}
        </div>
    );
}

