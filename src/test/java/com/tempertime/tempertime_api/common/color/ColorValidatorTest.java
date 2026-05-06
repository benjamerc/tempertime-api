package com.tempertime.tempertime_api.common.color;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ColorValidatorTest {

    private final ColorValidator colorValidator = new ColorValidator();

    @Test
    void shouldReturnTrue_whenColorIsNull() {
        assertThat(colorValidator.isColorMissing(null)).isTrue();
    }

    @Test
    void shouldReturnTrue_whenColorIsEmpty() {
        assertThat(colorValidator.isColorMissing("")).isTrue();
    }

    @Test
    void shouldReturnTrue_whenColorIsBlank() {
        assertThat(colorValidator.isColorMissing("   ")).isTrue();
    }

    @Test
    void shouldReturnFalse_whenColorIsPresent() {
        assertThat(colorValidator.isColorMissing("#ABC")).isFalse();
    }

    @Test
    void shouldReturnTrue_whenColorIsValidThreeCharHex() {
        assertThat(colorValidator.isHexColor("#ABC")).isTrue();
    }

    @Test
    void shouldReturnTrue_whenColorIsValidSixCharHex() {
        assertThat(colorValidator.isHexColor("#A3B4C5")).isTrue();
    }

    @Test
    void shouldReturnTrue_whenColorIsLowercase() {
        assertThat(colorValidator.isHexColor("#a3b4c5")).isTrue();
    }

    @Test
    void shouldReturnFalse_whenColorIsMissingHash() {
        assertThat(colorValidator.isHexColor("A3B4C5")).isFalse();
    }

    @Test
    void shouldReturnFalse_whenColorHasInvalidCharacters() {
        assertThat(colorValidator.isHexColor("#GGHHII")).isFalse();
    }

    @Test
    void shouldReturnFalse_whenColorHasInvalidLength() {
        assertThat(colorValidator.isHexColor("#A3B4")).isFalse();
    }

    @Test
    void shouldReturnFalse_whenColorIsPlainText() {
        assertThat(colorValidator.isHexColor("blue")).isFalse();
    }
}