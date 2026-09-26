package fr.maceun.api.common;

import java.util.Map;

public record ApiError(String error, String message, Map<String, String> fields) {

    public static ApiError of(String error, String message) {
        return new ApiError(error, message, Map.of());
    }
}
