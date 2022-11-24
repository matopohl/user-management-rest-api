package com.matopohl.user_management.service;

import com.matopohl.user_management.exception.custom.BaseException;
import com.matopohl.user_management.exception.custom.EntityConflictException;
import com.matopohl.user_management.exception.custom.NotFoundException;
import com.matopohl.user_management.model.request.AuthorityCreateRequest;
import com.matopohl.user_management.model.request.AuthorityPatchRequest;
import com.matopohl.user_management.model.request.AuthorityUpdateRequest;
import com.matopohl.user_management.model.response.AuthorityResponse;

import java.util.List;

public interface AuthorityService {

    List<AuthorityResponse> getAuthorities(Integer page, Integer size, String sort);

    AuthorityResponse getAuthority(String id) throws NotFoundException;

    AuthorityResponse createAuthority(AuthorityCreateRequest authorityCreateRequest) throws EntityConflictException;

    AuthorityResponse updateAuthority(AuthorityUpdateRequest authorityUpdateRequest, String id) throws NotFoundException, EntityConflictException;

    AuthorityResponse patchAuthority(AuthorityPatchRequest authorityPatchyRequest, String id) throws NotFoundException, EntityConflictException;

    void deleteAuthority(String id) throws BaseException;

}
