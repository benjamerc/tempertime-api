package com.tempertime.tempertime_api.events.service.rules;

import com.tempertime.tempertime_api.events.config.EventConstraintsProperties;
import com.tempertime.tempertime_api.events.exception.EventDateLimitExceededException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EventDateRulesTest {

    @Mock
    private EventConstraintsProperties properties;

    @InjectMocks
    private EventDateRules eventDateRules;

    @Test
    void shouldNotThrow_whenEventDateIsWithinAllowedRange() {

        when(properties.getMaxMonthsAhead()).thenReturn(12);

        OffsetDateTime eventDate = OffsetDateTime.now().plusMonths(6);

        assertThatCode(() -> eventDateRules.validateDateRange(eventDate))
                .doesNotThrowAnyException();

        verify(properties).getMaxMonthsAhead();
    }

    @Test
    void shouldThrowEventDateLimitExceededException_whenEventDateExceedsLimit() {

        when(properties.getMaxMonthsAhead()).thenReturn(12);

        OffsetDateTime eventDate = OffsetDateTime.now().plusMonths(24);

        assertThatThrownBy(() -> eventDateRules.validateDateRange(eventDate))
                .isInstanceOf(EventDateLimitExceededException.class);

        verify(properties).getMaxMonthsAhead();
    }
}