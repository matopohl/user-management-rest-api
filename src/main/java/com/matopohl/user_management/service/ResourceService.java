package com.matopohl.user_management.service;

import com.matopohl.user_management.exception.custom.BaseException;
import com.matopohl.user_management.exception.custom.EntityConflictException;
import com.matopohl.user_management.exception.custom.NotFoundException;
import com.matopohl.user_management.model.request.ResourceCreateRequest;
import com.matopohl.user_management.model.request.ResourcePatchRequest;
import com.matopohl.user_management.model.request.ResourceUpdateRequest;
import com.matopohl.user_management.model.response.ResourceResponse;

import java.util.List;

public interface ResourceService {

    List<ResourceResponse> getResources(Integer page, Integer size, String sort);

    ResourceResponse getResource(String id) throws NotFoundException;

    ResourceResponse createResource(ResourceCreateRequest resourceCreateRequest) throws EntityConflictException, NotFoundException;

    ResourceResponse updateResource(ResourceUpdateRequest resourceUpdateRequest, String id) throws NotFoundException, EntityConflictException;

    ResourceResponse patchResource(ResourcePatchRequest resourcePatchRequest, String id) throws NotFoundException, EntityConflictException;

    void deleteResource(String id) throws BaseException;

}
