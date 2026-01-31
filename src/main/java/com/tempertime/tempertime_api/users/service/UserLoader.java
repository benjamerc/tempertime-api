package com.tempertime.tempertime_api.users.service;

import com.tempertime.tempertime_api.users.exception.UserNotFoundException;
import com.tempertime.tempertime_api.users.model.User;
import com.tempertime.tempertime_api.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Loads User entities and throws a domain exception if not found */
@Service
@RequiredArgsConstructor
public class UserLoader {

    private final UserRepository userRepository;

    public User loadUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found with id: " + userId)
                );
    }
}
