interface TableSearchBarProps {
    searchTerm: string;
    onSearchChange: (value: string) => void;
    placeholder: string;
}

export function TableSearchBar({ searchTerm, onSearchChange, placeholder }: TableSearchBarProps) {
    return (
        <div className="search-container">
            <input
                type="text"
                placeholder={placeholder}
                value={searchTerm}
                onChange={(e) => onSearchChange(e.target.value)}
                className="search-input"
            />
        </div>
    );
}

