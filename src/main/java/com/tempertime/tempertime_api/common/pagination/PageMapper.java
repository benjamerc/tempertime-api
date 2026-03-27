package com.tempertime.tempertime_api.common.pagination;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;

/**
 * Utility class to map Spring Page objects to PageResponse DTOs.
 */
@UtilityClass
public class PageMapper {

    /**
     * Maps a Page of entities to a PageResponse DTO.
     */
    public static <T> PageResponse<T> toPageResponse(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
