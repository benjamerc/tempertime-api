package com.tempertime.tempertime_api.users.mapper;

import com.tempertime.tempertime_api.users.dto.response.UserProfileResponse;
import com.tempertime.tempertime_api.users.model.User;
import org.mapstruct.Mapper;

/** Maps user-related domain objects to DTOs */
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserProfileResponse toUserProfileResponse(User user);
}
