package multitier.trans.utils;

import multitier.trans.exception.ValidationException;

import java.util.List;

public class ValidationUtils {

    public static <T> void validateSequenceOrder(
            List<T> stops,
            int minStops,
            String resourceName,
            SequenceOrderGetter<T> sequenceOrderGetter
    ) {
        // Validate minimum stops
        if (stops == null || stops.size() < minStops) {
            throw new ValidationException(
                    String.format("%s must have at least %d %s", 
                            resourceName, 
                            minStops, 
                            minStops == 1 ? "stop" : "stops")
            );
        }

        // Validate sequence order is 0, 1, 2, ...
        for (int i = 0; i < stops.size(); i++) {
            T stop = stops.get(i);
            Integer sequenceOrder = sequenceOrderGetter.getSequenceOrder(stop);
            
            if (sequenceOrder == null || !sequenceOrder.equals(i)) {
                throw new ValidationException(
                        String.format("Invalid sequence order: expected %d but got %d", i, sequenceOrder)
                );
            }
        }
    }

    @FunctionalInterface
    public interface SequenceOrderGetter<T> {
        Integer getSequenceOrder(T stop);
    }
}

