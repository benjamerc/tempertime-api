package com.tempertime.tempertime_api.users.service.loader;

import com.tempertime.tempertime_api.users.data.UserTestDataProvider;
import com.tempertime.tempertime_api.users.domain.User;
import com.tempertime.tempertime_api.users.exception.UserNotFoundException;
import com.tempertime.tempertime_api.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserLoaderTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserLoader userLoader;

    @Test
    void shouldReturnUser_whenUserExists() {

        Long userId = 1L;
        User user = UserTestDataProvider.user(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userLoader.loadUserOrThrow(userId);

        assertThat(result).isEqualTo(user);

        verify(userRepository).findById(userId);
    }

    @Test
    void shouldThrowUserNotFoundException_whenUserDoesNotExist() {

        Long userId = 99L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userLoader.loadUserOrThrow(userId))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(userId);
    }
}
