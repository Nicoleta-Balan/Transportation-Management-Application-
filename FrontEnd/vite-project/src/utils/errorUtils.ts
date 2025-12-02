export function getErrorMessage(err: unknown, defaultMessage: string): string {
    return err instanceof Error ? err.message : defaultMessage;
}

