import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ResourceActionButtons } from '../ResourceActionButtons';

interface TestResource {
    id: number;
    name: string;
}

describe('ResourceActionButtons', () => {
    const testResource: TestResource = { id: 1, name: 'Test Resource' };
    const mockOnEditClick = vi.fn();
    const mockOnDeleteClick = vi.fn();

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('should render edit and delete buttons', () => {
        render(
            <ResourceActionButtons
                resource={testResource}
                editingId={null}
                deleting={null}
                deleteError={{}}
                onEditClick={mockOnEditClick}
                onDeleteClick={mockOnDeleteClick}
                getEditAriaLabel={(r) => `Edit ${r.name}`}
                getDeleteAriaLabel={(r) => `Delete ${r.name}`}
            />
        );

        const editButton = screen.getByLabelText('Edit Test Resource');
        const deleteButton = screen.getByLabelText('Delete Test Resource');

        expect(editButton).toBeInTheDocument();
        expect(deleteButton).toBeInTheDocument();
    });

    it('should call onEditClick when edit button is clicked', async () => {
        const user = userEvent.setup();

        render(
            <ResourceActionButtons
                resource={testResource}
                editingId={null}
                deleting={null}
                deleteError={{}}
                onEditClick={mockOnEditClick}
                onDeleteClick={mockOnDeleteClick}
                getEditAriaLabel={(r) => `Edit ${r.name}`}
                getDeleteAriaLabel={(r) => `Delete ${r.name}`}
            />
        );

        const editButton = screen.getByLabelText('Edit Test Resource');
        await user.click(editButton);

        expect(mockOnEditClick).toHaveBeenCalledWith(testResource);
    });

    it('should call onDeleteClick when delete button is clicked', async () => {
        const user = userEvent.setup();
        const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);

        render(
            <ResourceActionButtons
                resource={testResource}
                editingId={null}
                deleting={null}
                deleteError={{}}
                onEditClick={mockOnEditClick}
                onDeleteClick={mockOnDeleteClick}
                getEditAriaLabel={(r) => `Edit ${r.name}`}
                getDeleteAriaLabel={(r) => `Delete ${r.name}`}
            />
        );

        const deleteButton = screen.getByLabelText('Delete Test Resource');
        await user.click(deleteButton);

        expect(mockOnDeleteClick).toHaveBeenCalledWith(testResource);

        confirmSpy.mockRestore();
    });

    it('should disable edit button when another resource is being edited', () => {
        render(
            <ResourceActionButtons
                resource={testResource}
                editingId={2}
                deleting={null}
                deleteError={{}}
                onEditClick={mockOnEditClick}
                onDeleteClick={mockOnDeleteClick}
                getEditAriaLabel={(r) => `Edit ${r.name}`}
                getDeleteAriaLabel={(r) => `Delete ${r.name}`}
            />
        );

        const editButton = screen.getByLabelText('Edit Test Resource');
        expect(editButton).toBeDisabled();
    });

    it('should disable delete button when any resource is being edited', () => {
        render(
            <ResourceActionButtons
                resource={testResource}
                editingId={2}
                deleting={null}
                deleteError={{}}
                onEditClick={mockOnEditClick}
                onDeleteClick={mockOnDeleteClick}
                getEditAriaLabel={(r) => `Edit ${r.name}`}
                getDeleteAriaLabel={(r) => `Delete ${r.name}`}
            />
        );

        const deleteButton = screen.getByLabelText('Delete Test Resource');
        expect(deleteButton).toBeDisabled();
    });

    it('should disable buttons when this resource is being deleted', () => {
        render(
            <ResourceActionButtons
                resource={testResource}
                editingId={null}
                deleting={1}
                deleteError={{}}
                onEditClick={mockOnEditClick}
                onDeleteClick={mockOnDeleteClick}
                getEditAriaLabel={(r) => `Edit ${r.name}`}
                getDeleteAriaLabel={(r) => `Delete ${r.name}`}
            />
        );

        const editButton = screen.getByLabelText('Edit Test Resource');
        const deleteButton = screen.getByLabelText('Delete Test Resource');

        expect(editButton).toBeDisabled();
        expect(deleteButton).toBeDisabled();
    });

    it('should display delete error message', () => {
        render(
            <ResourceActionButtons
                resource={testResource}
                editingId={null}
                deleting={null}
                deleteError={{ 1: 'Delete failed' }}
                onEditClick={mockOnEditClick}
                onDeleteClick={mockOnDeleteClick}
                getEditAriaLabel={(r) => `Edit ${r.name}`}
                getDeleteAriaLabel={(r) => `Delete ${r.name}`}
            />
        );

        expect(screen.getByText('Delete failed')).toBeInTheDocument();
        expect(screen.getByText('Delete failed')).toHaveAttribute('role', 'alert');
    });

    it('should not display error for other resources', () => {
        render(
            <ResourceActionButtons
                resource={testResource}
                editingId={null}
                deleting={null}
                deleteError={{ 2: 'Delete failed' }}
                onEditClick={mockOnEditClick}
                onDeleteClick={mockOnDeleteClick}
                getEditAriaLabel={(r) => `Edit ${r.name}`}
                getDeleteAriaLabel={(r) => `Delete ${r.name}`}
            />
        );

        expect(screen.queryByText('Delete failed')).not.toBeInTheDocument();
    });
});

