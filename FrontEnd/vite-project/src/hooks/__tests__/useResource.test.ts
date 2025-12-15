import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor, act } from '@testing-library/react';
import { useResource } from '../useResource';

interface TestResource {
    id: number;
    name: string;
}

describe('useResource', () => {
    let mockApi: {
        getAll: ReturnType<typeof vi.fn>;
        delete: ReturnType<typeof vi.fn>;
    };

    beforeEach(() => {
        mockApi = {
            getAll: vi.fn(),
            delete: vi.fn(),
        };
    });

    it('should load data on mount by default', async () => {
        const testData: TestResource[] = [
            { id: 1, name: 'Resource 1' },
            { id: 2, name: 'Resource 2' },
        ];
        mockApi.getAll.mockResolvedValue(testData);

        const { result } = renderHook(() =>
            useResource(mockApi, { resourceName: 'test' })
        );

        expect(result.current.loading).toBe(true);
        expect(result.current.data).toEqual([]);

        await waitFor(() => {
            expect(result.current.loading).toBe(false);
        });

        expect(result.current.data).toEqual(testData);
        expect(result.current.error).toBeNull();
        expect(mockApi.getAll).toHaveBeenCalledTimes(1);
    });

    it('should handle loading errors', async () => {
        const error = new Error('Failed to load');
        mockApi.getAll.mockRejectedValue(error);

        const { result } = renderHook(() =>
            useResource(mockApi, { resourceName: 'test' })
        );

        await waitFor(() => {
            expect(result.current.loading).toBe(false);
        });

        expect(result.current.error).toContain('Failed to load');
        expect(result.current.data).toEqual([]);
    });

    it('should not load data on mount when loadOnMount is false', () => {
        const { result } = renderHook(() =>
            useResource(mockApi, {
                resourceName: 'test',
                loadOnMount: false,
            })
        );

        expect(result.current.loading).toBe(false);
        expect(mockApi.getAll).not.toHaveBeenCalled();
    });

    it('should handle delete with confirmation', async () => {
        const testData: TestResource[] = [{ id: 1, name: 'Resource 1' }];
        mockApi.getAll.mockResolvedValue(testData);
        mockApi.delete.mockResolvedValue(undefined);

        const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);

        const { result } = renderHook(() =>
            useResource<TestResource>(mockApi, {
                resourceName: 'test',
                getDeleteMessage: (item) => `Delete ${item.name}?`,
            })
        );

        await waitFor(() => {
            expect(result.current.loading).toBe(false);
        });

        result.current.handleDeleteClick(testData[0]);

        await waitFor(() => {
            expect(result.current.deleting).toBeNull();
        });

        expect(confirmSpy).toHaveBeenCalledWith('Delete Resource 1?');
        expect(mockApi.delete).toHaveBeenCalledWith(1);
        expect(mockApi.getAll).toHaveBeenCalledTimes(2); // Initial load + refresh after delete

        confirmSpy.mockRestore();
    });

    it('should not delete when confirmation is cancelled', async () => {
        const testData: TestResource[] = [{ id: 1, name: 'Resource 1' }];
        mockApi.getAll.mockResolvedValue(testData);

        const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(false);

        const { result } = renderHook(() =>
            useResource(mockApi, { resourceName: 'test' })
        );

        await waitFor(() => {
            expect(result.current.loading).toBe(false);
        });

        result.current.handleDeleteClick(testData[0]);

        expect(confirmSpy).toHaveBeenCalled();
        expect(mockApi.delete).not.toHaveBeenCalled();

        confirmSpy.mockRestore();
    });

    it('should handle delete errors', async () => {
        const testData: TestResource[] = [{ id: 1, name: 'Resource 1' }];
        mockApi.getAll.mockResolvedValue(testData);
        mockApi.delete.mockRejectedValue(new Error('Delete failed'));

        const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);

        const { result } = renderHook(() =>
            useResource(mockApi, { resourceName: 'test' })
        );

        await waitFor(() => {
            expect(result.current.loading).toBe(false);
        });

        result.current.handleDeleteClick(testData[0]);

        await waitFor(() => {
            expect(result.current.deleting).toBeNull();
        });

        await waitFor(() => {
            expect(result.current.deleteError[1]).toBeDefined();
        });

        expect(result.current.deleteError[1]).toContain('Delete failed');

        confirmSpy.mockRestore();
    });

    it('should allow manual reload', async () => {
        const testData: TestResource[] = [{ id: 1, name: 'Resource 1' }];
        mockApi.getAll.mockResolvedValue(testData);

        const { result } = renderHook(() =>
            useResource(mockApi, {
                resourceName: 'test',
                loadOnMount: false,
            })
        );

        expect(result.current.data).toEqual([]);

        await act(async () => {
            await result.current.load();
        });

        await waitFor(() => {
            expect(result.current.data).toEqual(testData);
        });

        expect(mockApi.getAll).toHaveBeenCalledTimes(1);
    });
});

