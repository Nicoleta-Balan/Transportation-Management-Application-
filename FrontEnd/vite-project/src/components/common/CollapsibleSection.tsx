import React, { useState } from 'react';

interface CollapsibleSectionProps {
    title: string;
    
    children: React.ReactNode;
    
    defaultExpanded?: boolean;
    
    isExpanded?: boolean;
    
    onToggle?: () => void;
    
    className?: string;
    
    headerClassName?: string;
    
    expandedIcon?: string;
    
    collapsedIcon?: string;
}

export function CollapsibleSection({
    title,
    children,
    defaultExpanded = false,
    isExpanded: controlledExpanded,
    onToggle,
    className = '',
    headerClassName = 'form-header-toggle',
    expandedIcon = '−',
    collapsedIcon = '+',
}: CollapsibleSectionProps): React.ReactElement {
    // Internal state for uncontrolled mode
    const [internalExpanded, setInternalExpanded] = useState(defaultExpanded);
    
    // Determine if controlled or uncontrolled
    const isControlled = controlledExpanded !== undefined;
    const isExpanded = isControlled ? controlledExpanded : internalExpanded;
    
    // Handle toggle click
    const handleToggle = () => {
        if (isControlled) {
            // Controlled mode: call parent's toggle handler
            onToggle?.();
        } else {
            // Uncontrolled mode: manage internal state
            setInternalExpanded(!internalExpanded);
        }
    };
    
    return (
        <section className={className}>
            <div 
                className={headerClassName}
                onClick={handleToggle}
            >
                <h2>{title}</h2>
                <button 
                    type="button" 
                    className="collapse-toggle"
                    aria-label={isExpanded ? 'Collapse section' : 'Expand section'}
                    aria-expanded={isExpanded}
                >
                    {isExpanded ? expandedIcon : collapsedIcon}
                </button>
            </div>
            
            {isExpanded && (
                <div className="section-content">
                    {children}
                </div>
            )}
        </section>
    );
}

