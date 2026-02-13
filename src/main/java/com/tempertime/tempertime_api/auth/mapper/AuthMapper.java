package com.tempertime.tempertime_api.auth.mapper;

import com.tempertime.tempertime_api.auth.dto.response.AuthRegisterResponse;
import com.tempertime.tempertime_api.users.domain.User;
import org.mapstruct.Mapper;

/**
 * Maps authentication domain objects to response DTOs.
 */
@Mapper(componentModel = "spring")
public interface AuthMapper {

    AuthRegisterResponse toAuthRegisterResponse(User user);
}
