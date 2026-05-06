package com.tempertime.tempertime_api.common.color;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ColorUtilTest {

    @Mock
    private ColorValidator colorValidator;

    @Mock
    private ColorGenerator colorGenerator;

    @Test
    void shouldReturnGeneratedColor_whenColorIsMissing() {

        when(colorValidator.isColorMissing(null)).thenReturn(true);
        when(colorGenerator.generate()).thenReturn("#A3B4C5");

        String result = ColorUtil.resolveColor(null, colorValidator, colorGenerator);

        assertThat(result).isEqualTo("#A3B4C5");
        verify(colorGenerator).generate();
        verify(colorValidator, never()).isHexColor(null);
    }

    @Test
    void shouldReturnProvidedColor_whenColorIsValidHex() {

        when(colorValidator.isColorMissing("#A3B4C5")).thenReturn(false);
        when(colorValidator.isHexColor("#A3B4C5")).thenReturn(true);

        String result = ColorUtil.resolveColor("#A3B4C5", colorValidator, colorGenerator);

        assertThat(result).isEqualTo("#A3B4C5");
        verify(colorGenerator, never()).generate();
    }

    @Test
    void shouldThrowInvalidColorFormatException_whenColorIsInvalidHex() {

        when(colorValidator.isColorMissing("blue")).thenReturn(false);
        when(colorValidator.isHexColor("blue")).thenReturn(false);

        assertThatThrownBy(() -> ColorUtil.resolveColor("blue", colorValidator, colorGenerator))
                .isInstanceOf(InvalidColorFormatException.class);

        verify(colorGenerator, never()).generate();
    }

    @Test
    void shouldNotThrow_whenValidateIfPresentAndColorIsMissing() {

        when(colorValidator.isColorMissing(null)).thenReturn(true);

        assertThatCode(() -> ColorUtil.validateIfPresent(null, colorValidator))
                .doesNotThrowAnyException();

        verify(colorValidator, never()).isHexColor(null);
    }

    @Test
    void shouldNotThrow_whenValidateIfPresentAndColorIsValidHex() {

        when(colorValidator.isColorMissing("#A3B4C5")).thenReturn(false);
        when(colorValidator.isHexColor("#A3B4C5")).thenReturn(true);

        assertThatCode(() -> ColorUtil.validateIfPresent("#A3B4C5", colorValidator))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowInvalidColorFormatException_whenValidateIfPresentAndColorIsInvalidHex() {

        when(colorValidator.isColorMissing("blue")).thenReturn(false);
        when(colorValidator.isHexColor("blue")).thenReturn(false);

        assertThatThrownBy(() -> ColorUtil.validateIfPresent("blue", colorValidator))
                .isInstanceOf(InvalidColorFormatException.class);
    }
}