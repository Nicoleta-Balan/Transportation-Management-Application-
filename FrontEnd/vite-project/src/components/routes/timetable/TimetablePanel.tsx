import { useState, useEffect } from 'react';
import type { Route } from '../../../types/Route';
import { useTimetables } from '../../../hooks/useTimetables';
import { CollapsibleSection } from '../../common/CollapsibleSection';
import { TimetableForm } from './TimetableForm';
import { TimetableTable } from './TimetableTable';
import { ErrorBoundary } from '../../ErrorBoundary';
import './TimetablePanel.css';

interface TimetablePanelProps {
    route: Route;
}

export function TimetablePanel({ route }: TimetablePanelProps) {
    const [isCreateFormExpanded, setIsCreateFormExpanded] = useState(false);
    const [editingTimetable, setEditingTimetable] = useState<number | null>(null);
    const timetablesData = useTimetables(route.id);

    // Load timetables when route changes
    useEffect(() => {
        if (route.id) {
            timetablesData.loadTimetables().catch((err) => {
                console.error('Failed to load timetables:', err);
            });
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [route.id]);

    return (
        <ErrorBoundary sectionName="Timetable Panel">
            <div className="timetable-panel">
                {/* Create New Timetable Form - Always visible */}
                <CollapsibleSection
                    title="Create New Timetable"
                    className="route-form-section timetable-form-section"
                    isExpanded={isCreateFormExpanded}
                    onToggle={() => setIsCreateFormExpanded(!isCreateFormExpanded)}
                >
                    <ErrorBoundary sectionName="Create Timetable Form">
                        <TimetableForm
                            route={route}
                            onSave={async () => {
                                await timetablesData.loadTimetables();
                                setIsCreateFormExpanded(false);
                            }}
                        />
                    </ErrorBoundary>
                </CollapsibleSection>

                {/* Timetables Table */}
                <ErrorBoundary sectionName="Timetables Table">
                    <TimetableTable
                        timetables={timetablesData.timetables}
                        loading={timetablesData.loading}
                        error={timetablesData.error}
                        deleting={timetablesData.deleting}
                        deleteError={timetablesData.deleteError}
                        editingTimetableId={editingTimetable}
                        route={route}
                        onDeleteClick={timetablesData.handleDeleteClick}
                        onEdit={async (timetable) => {
                            setEditingTimetable(timetable.id);
                            setIsCreateFormExpanded(false); // Collapse create form when editing
                        }}
                        onCancelEdit={() => setEditingTimetable(null)}
                        onSaveEdit={async () => {
                            await timetablesData.loadTimetables();
                            setEditingTimetable(null);
                        }}
                    />
                </ErrorBoundary>
            </div>
        </ErrorBoundary>
    );
}

