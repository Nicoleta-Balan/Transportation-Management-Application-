import React from 'react';
import type { Route } from '../../../types/Route';

import { calculateDuration } from '../../../utils/distanceCalculator';
import { useRouteEditFormContext } from '../../../contexts/RouteEditFormContext';
import type { RouteSortColumn } from '../../../hooks/useRouteFilters';

import { RouteEditForm } from '../form/RouteEditForm';
import RouteActionButtons from './RouteActionButtons';
import { TimetablePanel } from '../timetable/TimetablePanel';
import { TableSearchBar } from '../../common/TableSearchBar';
import { SortableTableHeader } from '../../common/SortableTableHeader';

interface RoutesTableProps {
    sortedAndFilteredRoutes: Route[];
    sortColumn: RouteSortColumn | null;
    sortDirection: 'asc' | 'desc';
    deleting: number | null;
    deleteError: Record<number, string>;
    searchTerm: string;
    onSearchChange: (value: string) => void;
    onSort: (column: RouteSortColumn) => void;
    onDeleteClick: (route: Route) => void;
    onTimetableClick?: (route: Route) => void;
    viewingTimetableRouteId?: number | null;
}

export default function RoutesTable({
    sortedAndFilteredRoutes,
    sortColumn,
    sortDirection,
    deleting,
    deleteError,
    searchTerm,
    onSearchChange,
    onSort,
    onDeleteClick,
    onTimetableClick,
    viewingTimetableRouteId = null,
}: RoutesTableProps) {
    // Get edit form state from context
        const {
            editingRoute,
            register,
            handleSubmit,
            errors,
            updating,
            error,
            selectedStations,
            segmentOverrides,
            calculatedDistance: editCalculatedDistance,
            calculatedDurationMinutes: editCalculatedDurationMinutes,
            onSegmentChange,
            onSubmit,
            resetForm,
        } = useRouteEditFormContext();

    // Get start and end stations from selected stations
    const startStation = selectedStations[0] || null;
    const endStation = selectedStations[selectedStations.length - 1] || null;
    // Helper function to get origin and destination stations from route
    const getRouteStations = (route: Route) => {
        if (route.routeStops && route.routeStops.length > 0) {
            const stops = route.routeStops;
            return {
                origin: stops[0].station,
                destination: stops[stops.length - 1].station,
            };
        }
        // Fallback for backward compatibility
        return {
            origin: route.originStation,
            destination: route.destinationStation,
        };
    };

    // Helper function to get distance and duration for a route
    const getRouteMetrics = (route: Route) => {
        const distance = route.distance ?? 0;
        const durationHours = route.durationMinutes 
            ? route.durationMinutes / 60 
            : calculateDuration(distance);
        return { distance, durationHours };
    };

    return (
        <div className="routes-table-container">
            <TableSearchBar
                searchTerm={searchTerm}
                onSearchChange={onSearchChange}
                placeholder="Search by any station, vehicle class, or description..."
            />
            <table className="routes-table">
                <thead>
                    <tr>
                        <SortableTableHeader
                            column="start"
                            label="Start"
                            sortColumn={sortColumn}
                            sortDirection={sortDirection}
                            onSort={onSort}
                        />
                        <SortableTableHeader
                            column="end"
                            label="End"
                            sortColumn={sortColumn}
                            sortDirection={sortDirection}
                            onSort={onSort}
                        />
                        <SortableTableHeader
                            column="distance"
                            label="Distance (km)"
                            sortColumn={sortColumn}
                            sortDirection={sortDirection}
                            onSort={onSort}
                        />
                        <SortableTableHeader
                            column="duration"
                            label="Duration"
                            sortColumn={sortColumn}
                            sortDirection={sortDirection}
                            onSort={onSort}
                        />
                        <th>Description</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {sortedAndFilteredRoutes.length === 0 ? (
                        <tr>
                            <td colSpan={6} style={{ textAlign: 'center', padding: '2rem', color: '#666' }}>
                                No routes match your search.
                            </td>
                        </tr>
                    ) : (
                        sortedAndFilteredRoutes.map((route) => {
                            const { distance, durationHours } = getRouteMetrics(route);
                            const isEditing = editingRoute?.id === route.id;
                            const showTimetable = !isEditing && viewingTimetableRouteId === route.id;

                            const handleRowClick = (e: React.MouseEvent<HTMLTableRowElement>) => {
                                // Don't toggle if clicking on action buttons or if editing
                                if (isEditing) return;
                                const target = e.target as HTMLElement;
                                // Check if click is on action buttons or their children
                                if (target.closest('.action-buttons-container') || target.closest('.action-buttons')) {
                                    return;
                                }
                                // Toggle timetable view
                                if (onTimetableClick) {
                                    onTimetableClick(route);
                                }
                            };

                            return (
                                <React.Fragment key={route.id}>
                                    <tr 
                                        id={`route-row-${route.id}`}
                                        onClick={handleRowClick}
                                        style={{ cursor: isEditing ? 'default' : 'pointer' }}
                                    >
                                        {isEditing ? (
                                            // Edit mode - form spans all columns
                                            <td colSpan={6}>
                                                    <RouteEditForm
                                                        register={register}
                                                        handleSubmit={handleSubmit}
                                                        errors={errors}
                                                        updating={updating}
                                                        error={error}
                                                        startStation={startStation}
                                                        endStation={endStation}
                                                        selectedStations={selectedStations}
                                                        calculatedDistance={editCalculatedDistance}
                                                        calculatedDurationMinutes={editCalculatedDurationMinutes}
                                                        segmentOverrides={segmentOverrides}
                                                        onSegmentChange={onSegmentChange}
                                                        onSubmit={onSubmit}
                                                        onCancel={resetForm}
                                                    />
                                            </td>
                                        ) : (
                                            // Normal mode - regular cells
                                            <>
                                                <td className="route-start">{getRouteStations(route).origin?.name || '-'}</td>
                                                <td className="route-end">{getRouteStations(route).destination?.name || '-'}</td>
                                                <td className="route-distance">{distance.toFixed(1)} km</td>
                                                <td className="route-duration">
                                                    {route.durationMinutes 
                                                        ? `${route.durationMinutes} min (${durationHours.toFixed(1)} h)`
                                                        : `${durationHours.toFixed(1)} h`
                                                    }
                                                </td>
                                                <td className="route-description">{route.description || '-'}</td>
                                                <td>
                                                    <RouteActionButtons
                                                        route={route}
                                                        deleting={deleting}
                                                        deleteError={deleteError}
                                                        onDeleteClick={onDeleteClick}
                                                    />
                                                </td>
                                            </>
                                        )}
                                    </tr>
                                    {showTimetable && (
                                        <tr className="timetable-subrow">
                                            <td colSpan={6}>
                                                <TimetablePanel route={route} />
                                            </td>
                                        </tr>
                                    )}
                                </React.Fragment>
                            );
                        })
                    )}
                </tbody>
            </table>
        </div>
    );
}

