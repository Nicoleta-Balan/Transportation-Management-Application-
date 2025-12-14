package multitier.trans.utils;

import org.springframework.beans.BeanUtils;

public final class HybridDtoMapper {

    private HybridDtoMapper() {
        // Utility class - prevent instantiation
    }

    public static <T> T mapSimpleFields(Object source, T target) {
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T> T mapSimpleFieldsToNew(Object source, Class<T> targetClass) {
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to create entity instance of type " + targetClass.getSimpleName() + 
                ". Ensure the class has a no-args constructor.", e);
        }
    }
}

