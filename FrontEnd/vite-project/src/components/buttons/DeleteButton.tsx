import { ButtonBase } from './ButtonBase';

interface DeleteButtonProps {
    onClick: () => void;
    disabled?: boolean;
    isDeleting?: boolean;
    deletingLabel?: string;
    ariaLabel?: string;
    className?: string;
}

export function DeleteButton({
    onClick,
    disabled = false,
    isDeleting = false,
    deletingLabel = 'Deleting...',
    ariaLabel,
    className,
}: DeleteButtonProps) {
    return (
        <ButtonBase
            variant="delete"
            onClick={onClick}
            disabled={disabled || isDeleting}
            className={className}
            aria-label={ariaLabel || 'Delete'}
        >
            {isDeleting ? deletingLabel : 'Delete'}
        </ButtonBase>
    );
}

