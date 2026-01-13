import { API_CONFIG } from '../config/api';
import { fetchWithErrorHandling } from '../utils/apiClient';

export const pricingApi = {
    async calculatePrice(routeId: number, vehicleClass: string, category: string = 'ADULT'): Promise<number> {
        return fetchWithErrorHandling<number>(
            `${API_CONFIG.BASE_URL}/api/pricing/calculate?routeId=${routeId}&vehicleClass=${vehicleClass}&category=${category}`
        );
    }
};
