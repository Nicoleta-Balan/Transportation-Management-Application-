import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { DataListContent } from '../DataListContent';

interface TestItem {
    id: number;
    name: string;
}

describe('DataListContent', () => {
    const testData: TestItem[] = [
        { id: 1, name: 'Item 1' },
        { id: 2, name: 'Item 2' },
    ];

    it('should show loading state when loading and no data', () => {
        render(
            <DataListContent
                loading={true}
                error={null}
                data={[]}
                emptyMessage="No items"
            >
                {(data) => <div>Items: {data.length}</div>}
            </DataListContent>
        );

        expect(screen.getByText('Loading...')).toBeInTheDocument();
    });

    it('should show error state when error and no data', () => {
        render(
            <DataListContent
                loading={false}
                error="Failed to load"
                data={[]}
                emptyMessage="No items"
            >
                {(data) => <div>Items: {data.length}</div>}
            </DataListContent>
        );

        expect(screen.getByText('Failed to load')).toBeInTheDocument();
    });

    it('should show empty state when no data', () => {
        render(
            <DataListContent
                loading={false}
                error={null}
                data={[]}
                emptyMessage="No items found"
            >
                {(data) => <div>Items: {data.length}</div>}
            </DataListContent>
        );

        expect(screen.getByText('No items found')).toBeInTheDocument();
    });

    it('should render children when data exists', () => {
        render(
            <DataListContent
                loading={false}
                error={null}
                data={testData}
                emptyMessage="No items"
            >
                {(data) => (
                    <div>
                        {data.map((item) => (
                            <div key={item.id}>{item.name}</div>
                        ))}
                    </div>
                )}
            </DataListContent>
        );

        expect(screen.getByText('Item 1')).toBeInTheDocument();
        expect(screen.getByText('Item 2')).toBeInTheDocument();
        expect(screen.queryByText('No items')).not.toBeInTheDocument();
    });

    it('should not show loading when data exists even if loading is true', () => {
        render(
            <DataListContent
                loading={true}
                error={null}
                data={testData}
                emptyMessage="No items"
            >
                {(data) => (
                    <div>
                        {data.map((item) => (
                            <div key={item.id}>{item.name}</div>
                        ))}
                    </div>
                )}
            </DataListContent>
        );

        expect(screen.queryByText('Loading...')).not.toBeInTheDocument();
        expect(screen.getByText('Item 1')).toBeInTheDocument();
    });
});

