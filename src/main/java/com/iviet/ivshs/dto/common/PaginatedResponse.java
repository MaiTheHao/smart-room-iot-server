package com.iviet.ivshs.dto.common;

import java.util.List;

public record PaginatedResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    public PaginatedResponse(List<T> content, int page, int size, long totalElements) {
        this(content, page, size, totalElements, size > 0 ? (int) Math.ceil((double) totalElements / size) : 0);
    }

    /** Tạo PaginatedResponse từ list đầy đủ bằng cách cắt slice theo page/size. */
    public static <T> PaginatedResponse<T> ofList(List<T> all, int page, int size) {
        long total = all.size();
        int from = Math.min(page * size, (int) total);
        int to = Math.min(from + size, (int) total);
        return new PaginatedResponse<>(all.subList(from, to), page, size, total);
    }
}

