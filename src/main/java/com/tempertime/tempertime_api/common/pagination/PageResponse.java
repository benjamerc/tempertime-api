package com.tempertime.tempertime_api.common.pagination;

import java.util.List;

/**
 * Generic DTO representing a paginated response.
 * Contains page content and pagination metadata.
 *
 * @param <T> the type of elements in the page content
 */
public record PageResponse<T>(

        List<T> content,
        int currentPage,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean last
) {}