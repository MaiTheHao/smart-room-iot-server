package com.iviet.ivshs.dto;

import org.springframework.http.HttpStatus;
import java.time.Instant;

public record ApiResponseV1<T>(
    int status,
    String message,
    T data,
    Instant timestamp
) {
    public static <T> ApiResponseV1<T> ok(T data) {
        return new ApiResponseV1<>(
                HttpStatus.OK.value(),
                "Success",
                data,
                Instant.now()
        );
    }

    public static <T> ApiResponseV1<T> created(T data) {
        return new ApiResponseV1<>(
                HttpStatus.CREATED.value(),
                "Created successfully",
                data,
                Instant.now()
        );
    }

    public static <T> ApiResponseV1<T> success(HttpStatus status, T data, String message) {
        return new ApiResponseV1<>(
                status.value(),
                message,
                data,
                Instant.now()
        );
    }

    public static <T> ApiResponseV1<T> error(HttpStatus status, String message) {
        return new ApiResponseV1<>(
                status.value(),
                message,
                null,
                Instant.now()
        );
    }
}