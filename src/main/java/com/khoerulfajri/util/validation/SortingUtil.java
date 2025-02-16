package com.khoerulfajri.util.validation;

public class SortingUtil {
    public static <T> String sortByValidation(Class<T> clazz, String sortBy, String defaultSortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return defaultSortBy;
        }

        try {
            clazz.getDeclaredField(sortBy);
            return sortBy;
        } catch (NoSuchFieldException e) {
            return defaultSortBy;
        }
    }
}
