package com.tempertime.tempertime_api.events.support;

import lombok.experimental.UtilityClass;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Test utility for generating future event dates used in HTTP request payloads
 * for controller and end-to-end tests.
 */
@UtilityClass
public class EventTestDateFactory {

    public String futureDate() {
        return OffsetDateTime.now(ZoneOffset.of("-03:00"))
                .plusDays(7)
                .withHour(10)
                .withMinute(30)
                .withSecond(0)
                .withNano(0)
                .toString();
    }
}
