package com.tempertime.tempertime_api.users.repository;

import com.tempertime.tempertime_api.users.UserTestDataProvider;
import com.tempertime.tempertime_api.users.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void shouldReturnUser_whenEmailExists() {

        User user = userRepository.save(UserTestDataProvider.user(null));

        Optional<User> result = userRepository.findByEmail(user.getEmail());

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void shouldReturnTrue_whenEmailExists() {

        User user = userRepository.save(UserTestDataProvider.user(null));

        boolean exists = userRepository.existsByEmail(user.getEmail());

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalse_whenEmailDoesNotExist() {

        boolean exists = userRepository.existsByEmail("nonexistent@mail.com");

        assertThat(exists).isFalse();
    }

    @Test
    void shouldReturnCorrectCount_whenIdsExist() {

        User user1 = userRepository.save(UserTestDataProvider.user(null));
        User user2 = userRepository.save(UserTestDataProvider.user(null));

        long count = userRepository.countByIdIn(
                List.of(user1.getId(), user2.getId())
        );

        assertThat(count).isEqualTo(2);
    }
}
