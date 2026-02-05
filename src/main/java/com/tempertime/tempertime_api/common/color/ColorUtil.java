package com.tempertime.tempertime_api.common.color;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ColorUtil {

    /** Returns a valid color: generates one if missing, otherwise validates hexadecimal format */
    public static String resolveColor(
            String color,
            ColorValidator validator,
            ColorGenerator generator
    ) {
        if (validator.isColorMissing(color)) {
            return generator.generate();
        } else if (!validator.isHexColor(color)) {
            throw new InvalidColorFormatException("Color must be hexadecimal");
        }
        return color;
    }

    /** Validates a color only if present; does not generate */
    public static void validateIfPresent(String color, ColorValidator validator) {
        if (!validator.isColorMissing(color) && !validator.isHexColor(color)) {
            throw new InvalidColorFormatException("Color must be hexadecimal");
        }
    }
}
