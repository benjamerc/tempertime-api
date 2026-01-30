package com.tempertime.tempertime_api.workspaces.support;

import com.tempertime.tempertime_api.workspaces.exception.InvalidColorFormatException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class WorkspaceColorUtil {

    /** Returns a valid color: generates one if missing, otherwise validates hexadecimal format */
    public static String resolveColor(
            String color,
            WorkspaceColorValidator validator,
            WorkspaceColorGenerator generator
    ) {
        if (validator.isColorMissing(color)) {
            return generator.generate();
        } else if (!validator.isHexColor(color)) {
            throw new InvalidColorFormatException("Color must be hexadecimal");
        }
        return color;
    }

    /** Validates a color only if present; does not generate */
    public static void validateIfPresent(String color, WorkspaceColorValidator validator) {
        if (!validator.isColorMissing(color) && !validator.isHexColor(color)) {
            throw new InvalidColorFormatException("Color must be hexadecimal");
        }
    }
}
