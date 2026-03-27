package com.tempertime.tempertime_api.common.pagination;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * Validates and adjusts a Pageable object according to PaginationProperties.
 */
@Component
@RequiredArgsConstructor
public class PaginationValidator {

    private final PaginationProperties paginationProperties;

    public Pageable validate(Pageable pageable) {

        int size = pageable.getPageSize();
        int page = pageable.getPageNumber();

        if (size > paginationProperties.getMaxPageSize()) {
            size = paginationProperties.getMaxPageSize();
        }

        if (page < 0) page = 0;

        Sort sort = pageable.getSort();

        return PageRequest.of(page, size, sort);
    }
}
