package com.iviet.ivshs.dto;

import java.util.List;

public record PaginatedResponseV1<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
    public PaginatedResponseV1(List<T> content, int page, int size, long totalElements) {
        this(
            content, 
            page, 
            size, 
            totalElements, 
            size > 0 ? (int) Math.ceil((double) totalElements / size) : 0
        );
    }
}