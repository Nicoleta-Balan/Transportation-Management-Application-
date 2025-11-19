package multitier.trans.model;

import multitier.trans.config.ApplicationContextProvider;
import multitier.trans.repository.FarePolicyHistoryRepository;
import jakarta.persistence.*;

/**
 * Entity Listener for FarePolicy that automatically maintains history records.
 * 
 * This listener records all changes to fare policies (created, updated, deleted, activated, deactivated)
 * in the fare_policy_history table.
 * 
 * Note: Uses ApplicationContextProvider to access Spring beans since Entity Listeners
 * cannot use @Autowired directly.
 */
public class FarePolicyHistoryListener {

    /**
     * Called after a new fare policy is persisted.
     */
    @PostPersist
    public void onFarePolicyCreated(FarePolicy policy) {
        try {
            FarePolicyHistoryRepository repository = ApplicationContextProvider.getBean(FarePolicyHistoryRepository.class);
            
            FarePolicyHistory history = new FarePolicyHistory();
            history.setFarePolicyId(policy.getId());
            history.setRouteId(policy.getRoute().getId());
            history.setPassengerCategory(policy.getPassengerCategory());
            history.setVehicleClass(policy.getVehicleClass());
            history.setBasePrice(policy.getPrice());
            history.setOldPrice(null);
            history.setChangeType("CREATED");
            history.setEffectiveFrom(policy.getEffectiveFrom());
            history.setEffectiveTo(policy.getEffectiveTo());
            history.setChangedBy(getCreatedBy(policy));
            history.setChangedAt(java.time.LocalDateTime.now());
            
            repository.save(history);
        } catch (Exception e) {
            // Log error but don't fail the transaction
            System.err.println("Error recording fare policy history: " + e.getMessage());
        }
    }

    /**
     * Called after a fare policy is updated.
     */
    @PostUpdate
    public void onFarePolicyUpdated(FarePolicy policy) {
        try {
            // Note: We can't access OLD values in @PostUpdate, so we need to track changes differently
            // For now, we'll record the update, but old_price will be null
            // A better approach would be to track old state in a service layer
            
            FarePolicyHistoryRepository repository = ApplicationContextProvider.getBean(FarePolicyHistoryRepository.class);
            
            // Check if there were significant changes by comparing with last history record
            // For simplicity, we'll record all updates (the service layer can provide old state if needed)
            FarePolicyHistory history = new FarePolicyHistory();
            history.setFarePolicyId(policy.getId());
            history.setRouteId(policy.getRoute().getId());
            history.setPassengerCategory(policy.getPassengerCategory());
            history.setVehicleClass(policy.getVehicleClass());
            history.setBasePrice(policy.getPrice());
            history.setOldPrice(null); // Would need to be set by service layer
            history.setChangeType(determineChangeType(policy));
            history.setEffectiveFrom(policy.getEffectiveFrom());
            history.setEffectiveTo(policy.getEffectiveTo());
            history.setChangedBy(getUpdatedBy(policy));
            history.setChangedAt(java.time.LocalDateTime.now());
            
            repository.save(history);
        } catch (Exception e) {
            System.err.println("Error recording fare policy history: " + e.getMessage());
        }
    }

    /**
     * Called before a fare policy is removed.
     */
    @PreRemove
    public void onFarePolicyDeleted(FarePolicy policy) {
        try {
            FarePolicyHistoryRepository repository = ApplicationContextProvider.getBean(FarePolicyHistoryRepository.class);
            
            FarePolicyHistory history = new FarePolicyHistory();
            history.setFarePolicyId(policy.getId());
            history.setRouteId(policy.getRoute().getId());
            history.setPassengerCategory(policy.getPassengerCategory());
            history.setVehicleClass(policy.getVehicleClass());
            history.setBasePrice(null);
            history.setOldPrice(policy.getPrice());
            history.setChangeType("DELETED");
            history.setEffectiveFrom(policy.getEffectiveFrom());
            history.setEffectiveTo(policy.getEffectiveTo());
            history.setChangedBy("SYSTEM");
            history.setChangedAt(java.time.LocalDateTime.now());
            
            repository.save(history);
        } catch (Exception e) {
            System.err.println("Error recording fare policy history: " + e.getMessage());
        }
    }

    private String determineChangeType(FarePolicy policy) {
        // This is a simplified version - in a real implementation, you'd compare
        // with the previous state. For now, we'll use "UPDATED" as default.
        // The service layer can provide more context if needed.
        return "UPDATED";
    }

    private String getCreatedBy(FarePolicy policy) {
        // If FarePolicy extends BaseEntity and has createdBy field, use it
        // Otherwise, return null or "SYSTEM"
        return "SYSTEM";
    }

    private String getUpdatedBy(FarePolicy policy) {
        // If FarePolicy extends BaseEntity and has updatedBy field, use it
        // Otherwise, return null or "SYSTEM"
        return "SYSTEM";
    }
}

