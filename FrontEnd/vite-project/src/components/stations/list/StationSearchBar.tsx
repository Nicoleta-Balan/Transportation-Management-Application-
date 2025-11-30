interface StationSearchBarProps {
    searchTerm: string;
    onSearchChange: (value: string) => void;
}

export default function StationSearchBar({ searchTerm, onSearchChange }: StationSearchBarProps) {
    return (
        <div className="search-container">
            <input
                type="text"
                placeholder="Search by name, description, or address..."
                value={searchTerm}
                onChange={(e) => onSearchChange(e.target.value)}
                className="search-input"
            />
        </div>
    );
}

