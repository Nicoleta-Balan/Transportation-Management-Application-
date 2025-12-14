export function formatTime(dateTimeString: string): string {
    try {
        // Parse as local time (no timezone conversion)
        // Format is YYYY-MM-DDTHH:mm:ss or YYYY-MM-DDTHH:mm
        const match = dateTimeString.match(/T(\d{2}):(\d{2})/);
        if (match) {
            const hours = parseInt(match[1], 10);
            const minutes = parseInt(match[2], 10);
            const date = new Date();
            date.setHours(hours, minutes, 0, 0);
            return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
        }
        // Fallback to Date parsing if format is different
        const date = new Date(dateTimeString);
        return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } catch {
        return dateTimeString;
    }
}

export function formatDate(dateString: string): string {
    try {
        // Extract date part if it's a datetime string
        const dateOnly = dateString.split('T')[0];
        const date = new Date(dateOnly);
        return date.toLocaleDateString();
    } catch {
        return dateString;
    }
}

export function formatDateTime(dateTimeString: string): string {
    try {
        const date = new Date(dateTimeString);
        return date.toLocaleString();
    } catch {
        return dateTimeString;
    }
}

export function extractDate(dateTimeString: string): string {
    try {
        return dateTimeString.split('T')[0];
    } catch {
        return '';
    }
}

export function extractTime(dateTimeString: string): string {
    try {
        const date = new Date(dateTimeString);
        const hh = String(date.getHours()).padStart(2, '0');
        const mm = String(date.getMinutes()).padStart(2, '0');
        return `${hh}:${mm}`;
    } catch {
        return '';
    }
}

