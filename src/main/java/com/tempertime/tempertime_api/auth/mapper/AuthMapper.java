package com.tempertime.tempertime_api.auth.mapper;

import com.tempertime.tempertime_api.auth.dto.response.AuthRegisterResponse;
import com.tempertime.tempertime_api.users.model.User;
import org.mapstruct.Mapper;

/** Maps auth-related domain objects to DTOs */
@Mapper(componentModel = "spring")
public interface AuthMapper {

    AuthRegisterResponse toAuthRegisterResponse(User user);
}
