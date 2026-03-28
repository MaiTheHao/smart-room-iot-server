package com.iviet.ivshs.dto;

import java.util.List;

public record PaginatedResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
    public PaginatedResponse(List<T> content, int page, int size, long totalElements) {
        this(
            content, 
            page, 
            size, 
            totalElements, 
            size > 0 ? (int) Math.ceil((double) totalElements / size) : 0
        );
    }
}