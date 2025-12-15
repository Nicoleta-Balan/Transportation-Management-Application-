interface SortableTableHeaderProps<T extends string> {
    column: T;
    label: string;
    sortColumn: T | null;
    sortDirection: 'asc' | 'desc';
    onSort: (column: T) => void;
}

export function SortableTableHeader<T extends string>({
    column,
    label,
    sortColumn,
    sortDirection,
    onSort,
}: SortableTableHeaderProps<T>) {
    return (
        <th onClick={() => onSort(column)} className="sortable-header">
            {label}
            {sortColumn === column && (
                <span className="sort-icon">{sortDirection === 'asc' ? '↑' : '↓'}</span>
            )}
        </th>
    );
}

