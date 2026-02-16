package com.tempertime.tempertime_api.common.validator;

import org.springframework.stereotype.Component;

/**
 * Validates password strength.
 * Rules:
 * - At least 8 characters
 * - At least one uppercase letter
 * - At least one lowercase letter
 * - At least one number
 * - No spaces allowed
 */
@Component
public class PasswordValidator {

    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)\\S{8,}$";

    public void validate(String password) {
        if (password == null || !password.matches(PASSWORD_PATTERN)) {
            throw new InvalidPasswordFormatException();
        }
    }
}
