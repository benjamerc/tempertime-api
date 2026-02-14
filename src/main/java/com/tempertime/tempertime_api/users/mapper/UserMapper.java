package com.tempertime.tempertime_api.users.mapper;

import com.tempertime.tempertime_api.users.dto.response.UserProfileResponse;
import com.tempertime.tempertime_api.users.domain.User;
import org.mapstruct.Mapper;

/**
 * Maps user domain objects to response DTOs.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserProfileResponse toUserProfileResponse(User user);
}
