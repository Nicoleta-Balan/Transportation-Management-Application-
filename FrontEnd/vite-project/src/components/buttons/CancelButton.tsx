import { ButtonBase } from './ButtonBase';

interface CancelButtonProps {
    onClick: () => void;
    disabled?: boolean;
    ariaLabel?: string;
    className?: string;
}

export function CancelButton({
    onClick,
    disabled = false,
    ariaLabel = 'Cancel',
    className,
}: CancelButtonProps) {
    return (
        <ButtonBase
            variant="cancel"
            onClick={onClick}
            disabled={disabled}
            className={className}
            aria-label={ariaLabel}
        >
            Cancel
        </ButtonBase>
    );
}

