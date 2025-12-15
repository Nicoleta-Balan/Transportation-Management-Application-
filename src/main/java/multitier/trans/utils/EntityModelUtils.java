package multitier.trans.utils;

import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.hateoas.EntityModel;

/**
 * Utility class for creating EntityModel instances with HATEOAS links.
 * Provides methods to wrap entities in EntityModel and add self-links for REST responses.
 */
public final class EntityModelUtils {

    private EntityModelUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates an EntityModel for an entity and adds a self-link.
     *
     * @param <T>        The entity type
     * @param entity     The entity to wrap
     * @param entityClass The class of the entity
     * @param entityLinks The RepositoryEntityLinks instance for link generation
     * @return EntityModel with the entity and self-link
     */
    public static <T> EntityModel<T> createEntityModel(
            T entity,
            Class<T> entityClass,
            RepositoryEntityLinks entityLinks) {
        
        EntityModel<T> resource = EntityModel.of(entity);
        Long id = extractId(entity);
        if (id != null) {
            resource.add(entityLinks.linkToItemResource(entityClass, id));
        }
        return resource;
    }

    /**
     * Creates an EntityModel for an entity and adds a self-link, with error handling.
     * If link generation fails, the EntityModel is returned without the link.
     *
     * @param <T>        The entity type
     * @param entity     The entity to wrap
     * @param entityClass The class of the entity
     * @param entityLinks The RepositoryEntityLinks instance for link generation
     * @return EntityModel with the entity and self-link (if link generation succeeds)
     */
    public static <T> EntityModel<T> createEntityModelSafe(
            T entity,
            Class<T> entityClass,
            RepositoryEntityLinks entityLinks) {
        
        EntityModel<T> resource = EntityModel.of(entity);
        Long id = extractId(entity);
        if (id != null) {
            try {
                resource.add(entityLinks.linkToItemResource(entityClass, id));
            } catch (Exception linkException) {
                // Link generation failed, continue without it
            }
        }
        return resource;
    }

    /**
     * Extracts the ID from an entity using reflection.
     * Assumes all entities have a getId() method that returns Long.
     *
     * @param entity The entity to extract ID from
     * @return The entity ID, or null if extraction fails
     */
    private static Long extractId(Object entity) {
        try {
            java.lang.reflect.Method getIdMethod = entity.getClass().getMethod("getId");
            Object id = getIdMethod.invoke(entity);
            return (Long) id;
        } catch (Exception e) {
            return null;
        }
    }
}

