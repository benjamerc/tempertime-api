package com.tempertime.tempertime_api.common.normalizer;

import org.springframework.stereotype.Component;

@Component
public class InputNormalizer {

    /**
     * Normalizes a string by trimming leading and trailing spaces.
     */
    public String normalize(String input) {
        if (input == null) return null;
        return input.trim();
    }
}
