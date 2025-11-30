import type { Station } from '../../../types/Station';

import StationForm from '../form/StationForm';
import ActionButtons from './ActionButtons';
import StationSearchBar from './StationSearchBar';
import { SortableTableHeader } from './SortableTableHeader';

import { useStationEditFormContext } from '../../../contexts/StationEditFormContext';

interface StationsTableProps {
    sortedAndFilteredStations: Station[];
    sortColumn: keyof Station | null;
    sortDirection: 'asc' | 'desc';
    deleting: number | null;
    deleteError: Record<number, string>;
    searchTerm: string;
    onSearchChange: (value: string) => void;
    onSort: (column: keyof Station) => void;
    onDeleteClick: (station: Station) => void;
}

export default function StationsTable({
    sortedAndFilteredStations,
    sortColumn,
    sortDirection,
    deleting,
    deleteError,
    searchTerm,
    onSearchChange,
    onSort,
    onDeleteClick,
}: StationsTableProps) {
    // Get edit form state from context
    const {
        editingStation,
        registerEdit,
        handleEditSubmitForm,
        watchEdit,
        editErrors,
        isEditValid,
        updating,
        editError,
        handleEditSubmit,
        handleCancelEdit,
    } = useStationEditFormContext();
    return (
        <div className="stations-table-container">
            <StationSearchBar searchTerm={searchTerm} onSearchChange={onSearchChange} />
            <table className="stations-table">
                <thead>
                <tr>
                    <SortableTableHeader
                        column="name"
                        label="Name"
                        sortColumn={sortColumn}
                        sortDirection={sortDirection}
                        onSort={onSort}
                    />
                    <SortableTableHeader
                        column="description"
                        label="Description"
                        sortColumn={sortColumn}
                        sortDirection={sortDirection}
                        onSort={onSort}
                    />
                    <SortableTableHeader
                        column="address"
                        label="Address"
                        sortColumn={sortColumn}
                        sortDirection={sortDirection}
                        onSort={onSort}
                    />
                    <SortableTableHeader
                        column="status"
                        label="Status"
                        sortColumn={sortColumn}
                        sortDirection={sortDirection}
                        onSort={onSort}
                    />
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                {sortedAndFilteredStations.length === 0 ? (
                    <tr>
                        <td colSpan={5} style={{ textAlign: 'center', padding: '2rem', color: '#666' }}>
                            No stations match your search.
                        </td>
                    </tr>
                ) : (
                    sortedAndFilteredStations.map((station) => (
                    <tr key={station.id}>
                        {editingStation?.id === station.id ? (
                            // Edit mode - form spans all columns
                            <td colSpan={5}>
                                <StationForm
                                    mode="edit"
                                    station={station}
                                    register={registerEdit}
                                    handleSubmit={handleEditSubmitForm}
                                    watch={watchEdit}
                                    errors={editErrors}
                                    isValid={isEditValid}
                                    updating={updating}
                                    editError={editError}
                                    onSubmit={handleEditSubmit}
                                    onCancel={handleCancelEdit}
                                />
                            </td>
                        ) : (
                            // Normal mode - regular cells
                            <>
                                <td className="station-name">{station.name}</td>
                                <td className="station-description">
                                    {station.description || <em>No description</em>}
                                </td>
                                <td className="station-address">
                                    {station.address || <em>No address</em>}
                                </td>
                                <td>
                                    <span className={`status-badge status-${station.status.toLowerCase()}`}>
                                        {station.status}
                                    </span>
                                </td>
                                <td>
                                    <ActionButtons
                                        station={station}
                                        deleting={deleting}
                                        deleteError={deleteError}
                                        onDeleteClick={onDeleteClick}
                                    />
                                </td>
                            </>
                        )}
                    </tr>
                    ))
                )}
                </tbody>
            </table>
        </div>
    );
}

