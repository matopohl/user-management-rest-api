package com.matopohl.user_management.mapper;

import com.matopohl.user_management.domain.RefreshToken;
import com.matopohl.user_management.model.response.RefreshTokenResponse;
import org.mapstruct.Mapper;

@SuppressWarnings("unused")
@Mapper(componentModel = "spring")
public interface RefreshTokenMapper {

    RefreshTokenResponse toRefreshTokenResponse(RefreshToken refreshToken, String accessToken);

}
