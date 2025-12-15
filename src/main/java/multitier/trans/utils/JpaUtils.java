package multitier.trans.utils;

import jakarta.persistence.EntityManager;
import java.util.Collection;

public final class JpaUtils {

    private JpaUtils() {
        // Utility class - prevent instantiation
    }

    public static void clearCollectionAndFlush(Collection<?> collection, EntityManager entityManager) {
        collection.clear();
        entityManager.flush();
    }

    public static void flush(EntityManager entityManager) {
        entityManager.flush();
    }
}

