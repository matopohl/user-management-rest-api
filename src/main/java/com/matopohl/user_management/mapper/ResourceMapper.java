package com.matopohl.user_management.mapper;

import com.matopohl.user_management.domain.Resource;
import com.matopohl.user_management.model.request.ResourceCreateRequest;
import com.matopohl.user_management.model.request.ResourcePatchRequest;
import com.matopohl.user_management.model.request.ResourceUpdateRequest;
import com.matopohl.user_management.model.response.ResourceResponse;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.ArrayList;

@SuppressWarnings("unused")
@Mapper(componentModel = "spring", uses = {AuthorityMapper.class})
public interface ResourceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "resourceAuthorities", ignore = true)
    Resource toResource(ResourceCreateRequest resourceCreateRequest);

    @AfterMapping
    default void toResourceAfterMapping(ResourceCreateRequest resourceCreateRequest, @MappingTarget Resource resource) {
        resource.setRequestMethod(resource.getRequestMethod().toUpperCase());
    }

    @Mapping(target = "resourceAuthorities", ignore = true)
    Resource toResource(ResourceUpdateRequest resourceUpdateRequest, String id);

    @AfterMapping
    default void toResourceAfterMapping(ResourceUpdateRequest resourceUpdateRequest, String id, @MappingTarget Resource resource) {
        resource.setRequestMethod(resource.getRequestMethod().toUpperCase());
    }

    default void toResource(ResourcePatchRequest resourcePatchRequest, Resource resource) {
        if(resourcePatchRequest.getRequestUrl() != null) {
            resource.setRequestUrl(resourcePatchRequest.getRequestUrl());
        }
        if(resourcePatchRequest.getRequestMethod() != null) {
            resource.setRequestMethod(resourcePatchRequest.getRequestMethod().toUpperCase());
        }
        if(resourcePatchRequest.getAuthorities() != null) {
            resource.setResourceAuthorities(new ArrayList<>());
        }
    }

    @Mapping(target = "authorities", source = "resourceAuthorities")
    ResourceResponse toResourceResponse(Resource resource);

}
