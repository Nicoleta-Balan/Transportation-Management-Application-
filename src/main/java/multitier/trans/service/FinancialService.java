package multitier.trans.service;

/**
 * Service Layer Interface for financial calculations.
 Computation Service
 */
public interface FinancialService {

    /**
     * This method will calculate the total revenue
     * based on all confirmed reservations.
     *
     * @return The total computed revenue.
     */
    double calculateTotalRevenue();

    // We can add more methods later, like calculateProfit(costs)
}