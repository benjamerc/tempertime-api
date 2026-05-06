package com.tempertime.tempertime_api.common.validator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PasswordValidatorTest {

    private final PasswordValidator passwordValidator = new PasswordValidator();

    @Test
    void shouldNotThrow_whenPasswordIsValid() {

        assertThatCode(() -> passwordValidator.validate("Password123"))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowInvalidPasswordFormatException_whenPasswordIsNull() {

        assertThatThrownBy(() -> passwordValidator.validate(null))
                .isInstanceOf(InvalidPasswordFormatException.class);
    }

    @Test
    void shouldThrowInvalidPasswordFormatException_whenPasswordIsTooShort() {

        assertThatThrownBy(() -> passwordValidator.validate("Pass1"))
                .isInstanceOf(InvalidPasswordFormatException.class);
    }

    @Test
    void shouldThrowInvalidPasswordFormatException_whenPasswordHasNoUppercase() {

        assertThatThrownBy(() -> passwordValidator.validate("password123"))
                .isInstanceOf(InvalidPasswordFormatException.class);
    }

    @Test
    void shouldThrowInvalidPasswordFormatException_whenPasswordHasNoLowercase() {

        assertThatThrownBy(() -> passwordValidator.validate("PASSWORD123"))
                .isInstanceOf(InvalidPasswordFormatException.class);
    }

    @Test
    void shouldThrowInvalidPasswordFormatException_whenPasswordHasNoNumber() {

        assertThatThrownBy(() -> passwordValidator.validate("PasswordABC"))
                .isInstanceOf(InvalidPasswordFormatException.class);
    }

    @Test
    void shouldThrowInvalidPasswordFormatException_whenPasswordHasSpaces() {

        assertThatThrownBy(() -> passwordValidator.validate("Password 123"))
                .isInstanceOf(InvalidPasswordFormatException.class);
    }
}