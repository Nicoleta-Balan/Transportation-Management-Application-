package multitier.trans.utils;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

public final class StopCollectionUtils {

    private StopCollectionUtils() {
        // Utility class - prevent instantiation
    }

    public static <TStop, TParent> void addStopsToCollection(
            Collection<TStop> collection,
            List<TStop> stops,
            TParent parent,
            BiConsumer<TStop, TParent> setParent) {
        
        // Only clear if collection is not empty (for updates)
        // For new entities, clearing an empty collection is unnecessary and can interfere with JPA tracking
        if (!collection.isEmpty()) {
            collection.clear();
        }
        
        // Add each stop, ensuring bidirectional relationship is set
        for (TStop stop : stops) {
            // Ensure bidirectional relationship is set
            // This check prevents unnecessary updates if already set correctly
            setParent.accept(stop, parent);
            collection.add(stop);
        }
    }
}

