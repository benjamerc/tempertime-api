package com.tempertime.tempertime_api.common.pagination;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for pagination settings.
 */
@Component
@ConfigurationProperties(prefix = "application.pagination")
@Getter
@Setter
public class PaginationProperties {

    private int maxPageSize;
}
