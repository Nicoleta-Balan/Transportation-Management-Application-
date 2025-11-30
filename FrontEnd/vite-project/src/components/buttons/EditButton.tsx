import { ButtonBase } from './ButtonBase';

interface EditButtonProps {
    onClick: () => void;
    disabled?: boolean;
    ariaLabel?: string;
    className?: string;
    label?: string;
}

export function EditButton({
    onClick,
    disabled = false,
    ariaLabel,
    className,
    label = 'Edit',
}: EditButtonProps) {
    return (
        <ButtonBase
            variant="edit"
            onClick={onClick}
            disabled={disabled}
            className={className}
            aria-label={ariaLabel || `Edit ${label}`}
        >
            {label}
        </ButtonBase>
    );
}

