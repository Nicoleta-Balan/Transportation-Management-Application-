import type { Station } from '../../../types/Station';

interface SortableTableHeaderProps {
    column: keyof Station;
    label: string;
    sortColumn: keyof Station | null;
    sortDirection: 'asc' | 'desc';
    onSort: (column: keyof Station) => void;
}

export function SortableTableHeader({
    column,
    label,
    sortColumn,
    sortDirection,
    onSort,
}: SortableTableHeaderProps) {
    return (
        <th onClick={() => onSort(column)} className="sortable-header">
            {label}
            {sortColumn === column && (
                <span className="sort-icon">{sortDirection === 'asc' ? '↑' : '↓'}</span>
            )}
        </th>
    );
}

