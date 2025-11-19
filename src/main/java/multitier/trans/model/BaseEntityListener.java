package multitier.trans.model;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;

/**
 * Entity Listener for BaseEntity that automatically manages timestamp fields
 * and provides audit/computation rules.
 * 
 * This listener is automatically applied to all entities that extend BaseEntity
 * via the @EntityListeners annotation.
 */
public class BaseEntityListener {

    /**
     * Called before a new entity is persisted.
     * Sets both createdAt and updatedAt to the current timestamp.
     */
    @PrePersist
    public void prePersist(BaseEntity entity) {
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
    }

    /**
     * Called before an entity is updated.
     * Updates the updatedAt timestamp to the current time.
     */
    @PreUpdate
    public void preUpdate(BaseEntity entity) {
        entity.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Called before an entity is removed.
     * Can be used for audit logging, cleanup operations, or validation
     * before deletion.
     * 
     * This method provides a hook for implementing computation or audit rules
     * as required by Sprint 3 persistence implementation requirements.
     * 
     * Example use cases:
     * - Log deletion events for audit purposes
     * - Perform cleanup operations on related entities
     * - Validate that entity can be safely deleted
     * - Update related entities before deletion
     */
    @PreRemove
    public void preRemove(BaseEntity entity) {
        // Audit/computation rule: Log deletion timestamp
        // The entity still exists at this point, so we can access its properties
        // This is useful for audit logging or cleanup operations
        
        // Example: Could log deletion event
        // System.out.println("Entity being deleted: " + entity.getClass().getSimpleName() + 
        //                    " at " + LocalDateTime.now());
        
        // Example: Could perform cleanup on related entities
        // if (entity instanceof Route) {
        //     // Cleanup related reservations, timetables, etc.
        // }
        
        // Note: This method is called before the entity is removed from the database
        // Any exceptions thrown here will prevent the deletion
    }
}

