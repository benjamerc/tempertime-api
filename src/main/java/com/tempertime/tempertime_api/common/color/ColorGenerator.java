package com.tempertime.tempertime_api.common.color;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

/** Generates a default color when none is provided */
@Component
public class ColorGenerator {

    private static final List<String> COLORS = List.of(
            "#1ABC9C",
            "#2ECC71",
            "#3498DB",
            "#9B59B6",
            "#E67E22",
            "#E74C3C",
            "#F1C40F"
    );

    private final Random random = new Random();

    public String generate() {
        return COLORS.get(random.nextInt(COLORS.size()));
    }
}
