package com.matopohl.user_management.mapper;

import com.matopohl.user_management.domain.Authority;
import com.matopohl.user_management.model.request.AuthorityCreateRequest;
import com.matopohl.user_management.model.request.AuthorityPatchRequest;
import com.matopohl.user_management.model.request.AuthorityUpdateRequest;
import com.matopohl.user_management.model.response.AuthorityResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@SuppressWarnings("unused")
@Mapper(componentModel = "spring")
public interface AuthorityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "resources", ignore = true)
    Authority toAuthority(AuthorityCreateRequest authorityCreateRequest);

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "resources", ignore = true)
    Authority toAuthority(AuthorityUpdateRequest authorityUpdateRequest, String id);

    default void toAuthority(AuthorityPatchRequest authorityPatchRequest, Authority authority) {
        if(authorityPatchRequest.getName() != null) {
            authority.setName(authorityPatchRequest.getName());
        }
    }

    AuthorityResponse toAuthorityResponse(Authority authority);

}
