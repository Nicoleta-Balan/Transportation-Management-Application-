import type { ReactNode } from 'react';
import { ErrorBoundary } from '../ErrorBoundary';
import { CollapsibleSection } from './CollapsibleSection';

interface ResourcePageProps {
    /** Page title */
    title: string;
    /** CSS class name for the page container */
    pageClassName: string;
    /** Map section component */
    mapSection: ReactNode;
    /** Create form section */
    createFormSection?: {
        title: string;
        className: string;
        isExpanded: boolean;
        onToggle: () => void;
        content: ReactNode;
    };
    /** List section component */
    listSection: ReactNode;
}

export function ResourcePage({
    title,
    pageClassName,
    mapSection,
    createFormSection,
    listSection,
}: ResourcePageProps) {
    return (
        <div className={pageClassName}>
            <h1>{title}</h1>

            {/* Map Section */}
            <section className={`${pageClassName.replace('-page', '')}-map-section`}>
                <ErrorBoundary sectionName={`${title} Map`}>
                    <div className={`${pageClassName.replace('-page', '')}-map-container`}>
                        {mapSection}
                    </div>
                </ErrorBoundary>
            </section>

            {/* Create Form Section */}
            {createFormSection && (
                <CollapsibleSection
                    title={createFormSection.title}
                    className={createFormSection.className}
                    isExpanded={createFormSection.isExpanded}
                    onToggle={createFormSection.onToggle}
                >
                    <ErrorBoundary sectionName={`${createFormSection.title} Form`}>
                        {createFormSection.content}
                    </ErrorBoundary>
                </CollapsibleSection>
            )}

            {/* List Section */}
            <section className={`${pageClassName.replace('-page', '')}-list-section`}>
                <ErrorBoundary sectionName={`${title} Table`}>
                    {listSection}
                </ErrorBoundary>
            </section>
        </div>
    );
}

