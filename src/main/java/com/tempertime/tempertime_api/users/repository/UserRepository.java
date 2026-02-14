package com.tempertime.tempertime_api.users.repository;

import com.tempertime.tempertime_api.users.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by email.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists with the given email.
     */
    boolean existsByEmail(String email);

    /**
     * Counts the number of users whose IDs are in the given list.
     */
    long countByIdIn(List<Long> userIds);
}
