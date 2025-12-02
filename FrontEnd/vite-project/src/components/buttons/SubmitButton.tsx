import { ButtonBase } from './ButtonBase';

export type SubmitMode = 'save' | 'update';

export interface SubmitButtonProps {
    mode?: SubmitMode;
    isSubmitting?: boolean;
    isValid?: boolean;
    submittingLabel?: string;
    ariaLabel?: string;
    className?: string;
}

const BUTTON_CONFIG: Record<SubmitMode, { label: string; submitting: string; aria: string }> = {
    save: {
        label: 'Save',
        submitting: 'Saving...',
        aria: 'Save',
    },
    update: {
        label: 'Update',
        submitting: 'Updating...',
        aria: 'Update',
    },
};

export function SubmitButton({
    mode = 'save',
    isSubmitting = false,
    isValid = true,
    submittingLabel,
    ariaLabel,
    className,
}: SubmitButtonProps) {
    const config = BUTTON_CONFIG[mode];
    const label = submittingLabel || config.submitting;
    const buttonText = isSubmitting ? label : config.label;
    const ariaLabelText = ariaLabel || config.aria;
    const variant = mode === 'save' ? 'save' : 'update'; // Replaces the need for separate SaveButton and UpdateButton components

    return (
        <ButtonBase
            variant={variant}
            disabled={isSubmitting || !isValid}
            className={className}
            aria-label={ariaLabelText}
            title={!isValid ? "Please fill in all required fields" : ""}
        >
            {buttonText}
        </ButtonBase>
    );
}

