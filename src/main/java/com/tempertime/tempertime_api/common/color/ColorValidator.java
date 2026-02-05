package com.tempertime.tempertime_api.common.color;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/** Validates color */
@Component
public class ColorValidator {

    private static final Pattern HEX_COLOR_PATTERN =
            Pattern.compile("^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$");

    public boolean isColorMissing(String color) {
        return color == null || color.trim().isEmpty();
    }

    public boolean isHexColor(String color) {
        return HEX_COLOR_PATTERN.matcher(color).matches();
    }
}
