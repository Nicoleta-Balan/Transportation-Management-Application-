export const API_CONFIG = {
    BASE_URL: import.meta.env.VITE_API_BASE_URL || '',
     // Result: '' (empty string) because .env not defined
     // Handled by proxy in vite.config.ts
     // Will be replaced by the actual URL in production
    TIMEOUT: 30000,
} as const;

