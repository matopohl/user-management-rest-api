package com.matopohl.user_management.service.impl;

import com.matopohl.user_management.configuration.constants.ExceptionMessageCode;
import com.matopohl.user_management.domain.Authority;
import com.matopohl.user_management.domain.Resource;
import com.matopohl.user_management.exception.custom.BaseException;
import com.matopohl.user_management.exception.custom.EntityConflictException;
import com.matopohl.user_management.exception.custom.NotFoundException;
import com.matopohl.user_management.mapper.ResourceMapper;
import com.matopohl.user_management.model.request.ResourceCreateRequest;
import com.matopohl.user_management.model.request.ResourcePatchRequest;
import com.matopohl.user_management.model.request.ResourceUpdateRequest;
import com.matopohl.user_management.model.response.ResourceResponse;
import com.matopohl.user_management.repository.AuthorityRepository;
import com.matopohl.user_management.repository.ResourceRepository;
import com.matopohl.user_management.service.ResourceService;
import com.matopohl.user_management.service.helper.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ResourceServiceImpl implements ResourceService {

    private final ResourceMapper resourceMapper;
    private final RequestService requestService;
    private final ResourceRepository resourceRepository;
    private final AuthorityRepository authorityRepository;

    private static final String REQUEST_FIELD_URL = "url";
    private static final String REQUEST_FIELD_METHOD = "method";
    private static final String REQUEST_FIELD_AUTHORITIES = "authorities#";

    @Override
    public List<ResourceResponse> getResources(Integer page, Integer size, String sort) {
        Pageable pageable = requestService.getPageable(page, size, sort);

        Page<Resource> resources = resourceRepository.findAll(pageable);

        return resources.stream().map(resourceMapper::toResourceResponse).collect(Collectors.toList());
    }

    @Override
    public ResourceResponse getResource(String id) throws NotFoundException {
        UUID uuid = getResourceUUID(id);

        Optional<Resource> checkResource = resourceRepository.findById(uuid);

        if (checkResource.isPresent()) {
            return resourceMapper.toResourceResponse(checkResource.get());
        }

        throw new NotFoundException(ExceptionMessageCode.RESOURCE_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND);
    }

    @Override
    public ResourceResponse createResource(ResourceCreateRequest resourceCreateRequest) throws EntityConflictException {
        EntityConflictException entityConflictException = new EntityConflictException();

        String url = resourceCreateRequest.getRequestUrl();

        if(url.endsWith("/")) {
            resourceCreateRequest.setRequestUrl(url.substring(0, url.length() - 1));
        }

        Optional<Resource> checkResource = resourceRepository.findByRequestUrlAndRequestMethod(resourceCreateRequest.getRequestUrl(), resourceCreateRequest.getRequestMethod());

        checkResource(checkResource, entityConflictException);

        Resource resource = resourceMapper.toResource(resourceCreateRequest);

        setAuthorities(resource, resourceCreateRequest.getAuthorities(), entityConflictException);

        if(entityConflictException.getErrors().size() > 0) {
            throw entityConflictException;
        }

        Resource savedResource = resourceRepository.save(resource);

        return resourceMapper.toResourceResponse(savedResource);
    }

    @Override
    public ResourceResponse updateResource(ResourceUpdateRequest resourceUpdateRequest, String id) throws NotFoundException, EntityConflictException {
        UUID uuid = getResourceUUID(id);

        Optional<Resource> checkResource = resourceRepository.findById(uuid);

        if (checkResource.isPresent()) {
            EntityConflictException entityConflictException = new EntityConflictException();

            String url = resourceUpdateRequest.getRequestUrl();

            if(url.endsWith("/")) {
                resourceUpdateRequest.setRequestUrl(url.substring(0, url.length() - 1));
            }

            Optional<Resource> checkResourceByUrlAndName = resourceRepository.findByRequestUrlAndRequestMethodAndIdNot(resourceUpdateRequest.getRequestUrl(), resourceUpdateRequest.getRequestMethod(), uuid);

            checkResource(checkResourceByUrlAndName, entityConflictException);

            Resource resource = resourceMapper.toResource(resourceUpdateRequest, id);

            setAuthorities(resource, resourceUpdateRequest.getAuthorities(), entityConflictException);

            if(entityConflictException.getErrors().size() > 0) {
                throw entityConflictException;
            }

            Resource savedResource = resourceRepository.save(resource);

            return resourceMapper.toResourceResponse(savedResource);
        }

        throw new NotFoundException(ExceptionMessageCode.RESOURCE_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND);
    }

    @Override
    public ResourceResponse patchResource(ResourcePatchRequest resourcePatchRequest, String id) throws NotFoundException, EntityConflictException {
        UUID uuid = getResourceUUID(id);

        Optional<Resource> checkResource = resourceRepository.findById(uuid);

        if (checkResource.isPresent()) {
            EntityConflictException entityConflictException = new EntityConflictException();

            String url = resourcePatchRequest.getRequestUrl();

            if(url.endsWith("/")) {
                resourcePatchRequest.setRequestUrl(url.substring(0, url.length() - 1));
            }

            Optional<Resource> checkResourceByUrlAndName = resourceRepository.findByRequestUrlAndRequestMethodAndIdNot(resourcePatchRequest.getRequestUrl(), resourcePatchRequest.getRequestMethod(), uuid);

            checkResource(checkResourceByUrlAndName, entityConflictException);

            Resource resource = checkResource.get();

            resourceMapper.toResource(resourcePatchRequest, resource);

            setAuthorities(resource, resourcePatchRequest.getAuthorities(), entityConflictException);

            if(entityConflictException.getErrors().size() > 0) {
                throw entityConflictException;
            }

            Resource savedResource = resourceRepository.save(resource);

            return resourceMapper.toResourceResponse(savedResource);
        }

        throw new NotFoundException(ExceptionMessageCode.RESOURCE_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND);
    }

    @Override
    public void deleteResource(String id) throws BaseException {
        UUID uuid = getResourceUUID(id);

        Optional<Resource> checkResource = resourceRepository.findById(uuid);

        if (checkResource.isPresent()) {
            try {
                checkResource.ifPresent(resource -> resourceRepository.deleteById(resource.getId()));

                return;
            }
            catch(DataIntegrityViolationException ex) {
                throw new BaseException(ExceptionMessageCode.RESOURCE_CANNOT_DELETE, new String[]{id}, HttpStatus.CONFLICT, ex);
            }
        }

        throw new NotFoundException(ExceptionMessageCode.RESOURCE_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND);
    }

    private UUID getResourceUUID(String id) throws NotFoundException {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new NotFoundException(ExceptionMessageCode.RESOURCE_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND, ex);
        }
    }

    private void checkResource(Optional<Resource> checkResource, EntityConflictException ex) {
        if (checkResource.isPresent()) {
            ex.addError(ExceptionMessageCode.RESOURCE_ALREADY_EXISTS_BY_URL_AND_METHOD, new String[]{checkResource.get().getRequestUrl(), checkResource.get().getRequestMethod()}, REQUEST_FIELD_URL);
            ex.addError(ExceptionMessageCode.RESOURCE_ALREADY_EXISTS_BY_URL_AND_METHOD, new String[]{checkResource.get().getRequestUrl(), checkResource.get().getRequestMethod()}, REQUEST_FIELD_METHOD);
        }
    }

    private void setAuthorities(Resource resource, List<String> authorities, EntityConflictException entityConflictException) {
        if(authorities != null) {
            for (int i = 0; i < authorities.size(); i++) {
                String authorityId = authorities.get(i);

                UUID authorityUuid;

                try {
                    authorityUuid = UUID.fromString(authorityId);
                } catch (IllegalArgumentException ex) {
                    entityConflictException.addError(ExceptionMessageCode.AUTHORITY_NOT_FOUND, new String[]{authorityId}, REQUEST_FIELD_AUTHORITIES + i);

                    continue;
                }

                Optional<Authority> authority = authorityRepository.findById(authorityUuid);

                if (authority.isPresent()) {
                    resource.getResourceAuthorities().add(authority.get());
                } else {
                    entityConflictException.addError(ExceptionMessageCode.AUTHORITY_NOT_FOUND, new String[]{authorityId}, REQUEST_FIELD_AUTHORITIES + i);
                }
            }
        }
    }

}
