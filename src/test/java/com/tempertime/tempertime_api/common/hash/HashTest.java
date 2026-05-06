package com.tempertime.tempertime_api.common.hash;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HashTest {

    @Test
    void shouldReturnSha256Hash_whenValidInputProvided() {

        String result = Hash.sha256("hello");

        assertThat(result).isNotNull().isNotBlank();
        assertThat(result).hasSize(64);
    }

    @Test
    void shouldReturnConsistentHash_whenSameInputProvided() {

        String first = Hash.sha256("tempertime");
        String second = Hash.sha256("tempertime");

        assertThat(first).isEqualTo(second);
    }

    @Test
    void shouldReturnDifferentHashes_whenDifferentInputsProvided() {

        String first = Hash.sha256("input1");
        String second = Hash.sha256("input2");

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void shouldReturnLowercaseHexString() {

        String result = Hash.sha256("test");

        assertThat(result).matches("^[0-9a-f]{64}$");
    }
}
