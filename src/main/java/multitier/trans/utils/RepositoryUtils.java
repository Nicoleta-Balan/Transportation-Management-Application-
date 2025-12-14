package multitier.trans.utils;

import multitier.trans.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.function.Function;

public final class RepositoryUtils {

    private RepositoryUtils() {
        // Utility class - prevent instantiation
    }

    public static <T> T findByIdOrThrow(Optional<T> optional, String entityName, Long id) {
        return optional.orElseThrow(() -> new ResourceNotFoundException(entityName, id));
    }

    public static <T> T reloadWithStops(Function<Long, Optional<T>> findByIdWithStops, String entityName, Long id) {
        return findByIdOrThrow(findByIdWithStops.apply(id), entityName, id);
    }

    public static <T, ID> void deleteByIdOrThrow(JpaRepository<T, ID> repository, String entityName, ID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(entityName, id);
        }
        repository.deleteById(id);
    }
}

