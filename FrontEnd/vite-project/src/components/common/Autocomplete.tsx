import React, { useState, useEffect, useRef } from 'react';
import './Autocomplete.css';

interface Suggestion {
    id: number;
    name: string;
}

interface AutocompleteProps {
    suggestions: Suggestion[];
    onSelect: (suggestion: Suggestion) => void;
    placeholder?: string;
}

export const Autocomplete: React.FC<AutocompleteProps> = ({ suggestions, onSelect, placeholder }) => {
    const [inputValue, setInputValue] = useState('');
    const [filteredSuggestions, setFilteredSuggestions] = useState<Suggestion[]>([]);
    const [showSuggestions, setShowSuggestions] = useState(false);
    const wrapperRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (wrapperRef.current && !wrapperRef.current.contains(event.target as Node)) {
                setShowSuggestions(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, []);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const value = e.target.value;
        setInputValue(value);

        if (value.length > 0) {
            const filtered = suggestions.filter(suggestion =>
                suggestion.name.toLowerCase().includes(value.toLowerCase())
            );
            setFilteredSuggestions(filtered);
            setShowSuggestions(true);
        } else {
            setFilteredSuggestions([]);
            setShowSuggestions(false);
        }
    };

    const handleSelect = (suggestion: Suggestion) => {
        setInputValue(suggestion.name);
        onSelect(suggestion);
        setShowSuggestions(false);
        setFilteredSuggestions([]);
    };

    return (
        <div className="autocomplete-wrapper" ref={wrapperRef}>
            <input
                type="text"
                value={inputValue}
                onChange={handleChange}
                onFocus={() => setShowSuggestions(inputValue.length > 0)}
                placeholder={placeholder || 'Type to search...'}
                className="autocomplete-input"
            />
            {showSuggestions && filteredSuggestions.length > 0 && (
                <ul className="autocomplete-suggestions">
                    {filteredSuggestions.map(suggestion => (
                        <li
                            key={suggestion.id}
                            onClick={() => handleSelect(suggestion)}
                            className="suggestion-item"
                        >
                            {suggestion.name}
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
};
