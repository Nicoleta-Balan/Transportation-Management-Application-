import React from 'react';
import type { Timetable } from '../../../types/Timetable';
import type { Route } from '../../../types/Route';
import { EditButton, DeleteButton } from '../../buttons';
import { DataListContent } from '../../common/DataListContent';
import { TimetableForm } from './TimetableForm';
import { ErrorBoundary } from '../../ErrorBoundary';
import { formatTime, formatDate } from '../../../utils/dateTimeUtils';

interface TimetableTableProps {
    timetables: Timetable[];
    loading: boolean;
    error: string | null;
    deleting: number | null;
    deleteError: Record<number, string>;
    editingTimetableId?: number | null;
    route: Route;
    onDeleteClick: (timetable: Timetable) => void;
    onEdit: (timetable: Timetable) => Promise<void>;
    onCancelEdit: () => void;
    onSaveEdit: () => Promise<void>;
}

export function TimetableTable({
    timetables,
    loading,
    error,
    deleting,
    deleteError,
    editingTimetableId,
    route,
    onDeleteClick,
    onEdit,
    onCancelEdit,
    onSaveEdit,
}: TimetableTableProps) {
    // Date/time formatting functions are now imported from dateTimeUtils

    const getStartDate = (timetable: Timetable): string => {
        if (timetable.startDate) {
            return timetable.startDate;
        }
        if (timetable.timetableStops && timetable.timetableStops.length > 0) {
            const firstStop = timetable.timetableStops[0];
            const timeStr = firstStop.departureTime || firstStop.arrivalTime;
            if (timeStr) {
                const date = new Date(timeStr);
                return date.toISOString().split('T')[0];
            }
        }
        return '-';
    };

    const getEndDate = (timetable: Timetable): string => {
        if (timetable.endDate) {
            return timetable.endDate;
        }
        if (timetable.timetableStops && timetable.timetableStops.length > 0) {
            const lastStop = timetable.timetableStops[timetable.timetableStops.length - 1];
            if (lastStop.arrivalTime) {
                const date = new Date(lastStop.arrivalTime);
                return date.toISOString().split('T')[0];
            }
        }
        return '-';
    };

    const dayOptions = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

    return (
        <DataListContent
            loading={loading}
            error={error}
            data={timetables}
            emptyMessage="No timetables found. Create one above!"
        >
            {(data) => (
                <table className="timetables-table">
                    <thead>
                        <tr>
                            <th>Start Station</th>
                            <th>Departure Time</th>
                            <th>End Station</th>
                            <th>Arrival Time</th>
                            <th>Active Since</th>
                            <th>Active Until</th>
                            <th>Description</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {data.map((timetable) => {
                            try {
                                if (!timetable || !timetable.timetableStops || timetable.timetableStops.length === 0) {
                                    return (
                                        <tr key={timetable?.id || 'unknown'}>
                                            <td colSpan={8} style={{ textAlign: 'center', color: '#999' }}>
                                                No stops found for this timetable
                                            </td>
                                        </tr>
                                    );
                                }

                                const startStop = timetable.timetableStops[0];
                                const endStop = timetable.timetableStops[timetable.timetableStops.length - 1];
                                const isDeleting = deleting === timetable.id;

                            const isEditing = editingTimetableId === timetable.id;

                            return (
                                <React.Fragment key={timetable.id}>
                                    <tr>
                                        <td>{startStop?.station?.name || '-'}</td>
                                        <td>
                                            {startStop?.departureTime 
                                                ? formatTime(startStop.departureTime)
                                                : startStop?.arrivalTime
                                                    ? formatTime(startStop.arrivalTime)
                                                    : '-'
                                            }
                                        </td>
                                        <td>{endStop?.station?.name || '-'}</td>
                                        <td>
                                            {endStop?.arrivalTime 
                                                ? formatTime(endStop.arrivalTime)
                                                : '-'
                                            }
                                        </td>
                                        <td>{getStartDate(timetable) !== '-' ? formatDate(getStartDate(timetable)) : '-'}</td>
                                        <td>{getEndDate(timetable) !== '-' ? formatDate(getEndDate(timetable)) : '-'}</td>
                                        <td>{timetable.description || '-'}</td>
                                        <td>
                                            <div className="action-buttons-container">
                                                <div className="action-buttons">
                                                    <EditButton
                                                        onClick={() => onEdit(timetable)}
                                                        disabled={isDeleting || isEditing}
                                                        ariaLabel={`Edit timetable ${timetable.id}`}
                                                    />
                                                    <DeleteButton
                                                        onClick={() => onDeleteClick(timetable)}
                                                        disabled={isDeleting || isEditing}
                                                        isDeleting={isDeleting}
                                                        ariaLabel={`Delete timetable ${timetable.id}`}
                                                    />
                                                </div>
                                                {deleteError[timetable.id] && (
                                                    <div className="delete-error-message" role="alert">
                                                        {deleteError[timetable.id]}
                                                    </div>
                                                )}
                                            </div>
                                        </td>
                                    </tr>
                                    {/* Days checkboxes sub-row */}
                                    <tr className="timetable-days-subrow">
                                        <td colSpan={8} style={{ padding: '0.25rem 0.75rem 0.5rem 0.75rem' }}>
                                            <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
                                                <span style={{ fontSize: '0.85rem', fontWeight: '500', color: '#666' }}>Days:</span>
                                                <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
                                                    {dayOptions.map((day) => (
                                                        <label
                                                            key={day}
                                                            style={{
                                                                display: 'flex',
                                                                alignItems: 'center',
                                                                gap: '0.25rem',
                                                                fontSize: '0.75rem',
                                                                color: '#666',
                                                                cursor: 'not-allowed',
                                                                opacity: 0.7,
                                                            }}
                                                        >
                                                            <input
                                                                type="checkbox"
                                                                checked={timetable.daysOfWeek?.includes(day) || false}
                                                                disabled
                                                                readOnly
                                                                style={{
                                                                    cursor: 'not-allowed',
                                                                    opacity: 0.6,
                                                                }}
                                                            />
                                                            <span>{day}</span>
                                                        </label>
                                                    ))}
                                                </div>
                                            </div>
                                        </td>
                                    </tr>
                                    {isEditing && (
                                        <tr className="timetable-edit-subrow">
                                            <td colSpan={8}>
                                                <div className="route-form-section timetable-form-section">
                                                    <ErrorBoundary sectionName="Edit Timetable Form">
                                                        <TimetableForm
                                                            route={route}
                                                            timetable={timetable}
                                                            onSave={onSaveEdit}
                                                            onCancel={onCancelEdit}
                                                        />
                                                    </ErrorBoundary>
                                                </div>
                                            </td>
                                        </tr>
                                    )}
                                </React.Fragment>
                            );
                            } catch (err) {
                                console.error(`Error rendering timetable ${timetable?.id}:`, err);
                                return (
                                    <tr key={timetable?.id || 'error'}>
                                        <td colSpan={8} style={{ textAlign: 'center', color: '#dc3545' }}>
                                            Error displaying timetable: {err instanceof Error ? err.message : 'Unknown error'}
                                        </td>
                                    </tr>
                                );
                            }
                        })}
                    </tbody>
                </table>
            )}
        </DataListContent>
    );
}

