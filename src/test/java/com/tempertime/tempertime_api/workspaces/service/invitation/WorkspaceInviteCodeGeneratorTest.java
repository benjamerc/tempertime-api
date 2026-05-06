package com.tempertime.tempertime_api.workspaces.service.invitation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class WorkspaceInviteCodeGeneratorTest {

    private final WorkspaceInviteCodeGenerator generator = new WorkspaceInviteCodeGenerator();

    private static final Pattern VALID_CHARSET = Pattern.compile("^[A-Z0-9]+$");

    @Test
    void shouldGenerateInviteCodeOfCorrectLength() {

        String result = generator.generate();

        assertThat(result).hasSize(12);
    }

    @Test
    void shouldGenerateInviteCodeWithValidCharacters() {

        String result = generator.generate();

        assertThat(result).matches(VALID_CHARSET);
    }

    @Test
    void shouldGenerateDifferentInviteCodesOnSuccessiveCalls() {

        String first = generator.generate();
        String second = generator.generate();

        assertThat(first).isNotEqualTo(second);
    }
}
