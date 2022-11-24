package com.matopohl.user_management.service.impl;

import com.matopohl.user_management.configuration.constants.ExceptionMessageCode;
import com.matopohl.user_management.domain.Authority;
import com.matopohl.user_management.exception.custom.EntityConflictException;
import com.matopohl.user_management.exception.custom.BaseException;
import com.matopohl.user_management.exception.custom.NotFoundException;
import com.matopohl.user_management.mapper.AuthorityMapper;
import com.matopohl.user_management.model.request.AuthorityCreateRequest;
import com.matopohl.user_management.model.request.AuthorityPatchRequest;
import com.matopohl.user_management.model.request.AuthorityUpdateRequest;
import com.matopohl.user_management.model.response.AuthorityResponse;
import com.matopohl.user_management.repository.AuthorityRepository;
import com.matopohl.user_management.service.AuthorityService;
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
public class AuthorityServiceImpl implements AuthorityService {

    private final AuthorityMapper authorityMapper;
    private final RequestService requestService;
    private final AuthorityRepository authorityRepository;

    private static final String REQUEST_FIELD_NAME = "name";

    @Override
    public List<AuthorityResponse> getAuthorities(Integer page, Integer size, String sort) {
        Pageable pageable = requestService.getPageable(page, size, sort);

        Page<Authority> authorities = authorityRepository.findAll(pageable);

        return authorities.stream().map(authorityMapper::toAuthorityResponse).collect(Collectors.toList());
    }

    @Override
    public AuthorityResponse getAuthority(String id) throws NotFoundException {
        UUID uuid = getAuthorityUUID(id);

        Optional<Authority> checkAuthority = authorityRepository.findById(uuid);

        if (checkAuthority.isPresent()) {
            return authorityMapper.toAuthorityResponse(checkAuthority.get());
        }

        throw new NotFoundException(ExceptionMessageCode.AUTHORITY_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND);
    }

    @Override
    public AuthorityResponse createAuthority(AuthorityCreateRequest authorityCreateRequest) throws EntityConflictException {
        Optional<Authority> checkAuthority = authorityRepository.findByName(authorityCreateRequest.getName());

        checkAuthority(checkAuthority);

        Authority authority = authorityMapper.toAuthority(authorityCreateRequest);

        Authority savedAuthority = authorityRepository.save(authority);

        return authorityMapper.toAuthorityResponse(savedAuthority);
    }

    @Override
    public AuthorityResponse updateAuthority(AuthorityUpdateRequest authorityUpdateRequest, String id) throws NotFoundException, EntityConflictException {
        UUID uuid = UUID.fromString(id);

        Optional<Authority> checkAuthority = authorityRepository.findById(uuid);

        if (checkAuthority.isPresent()) {
            Optional<Authority> checkAuthorityByName = authorityRepository.findByNameAndIdNot(authorityUpdateRequest.getName(), uuid);

            checkAuthority(checkAuthorityByName);

            Authority authority = authorityMapper.toAuthority(authorityUpdateRequest, id);

            Authority savedAuthority = authorityRepository.save(authority);

            return authorityMapper.toAuthorityResponse(savedAuthority);
        }

        throw new NotFoundException(ExceptionMessageCode.AUTHORITY_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND);
    }

    @Override
    public AuthorityResponse patchAuthority(AuthorityPatchRequest authorityPatchRequest, String id) throws NotFoundException, EntityConflictException {
        UUID uuid = getAuthorityUUID(id);

        Optional<Authority> checkAuthority = authorityRepository.findById(uuid);

        if (checkAuthority.isPresent()) {
            Optional<Authority> checkAuthorityByName = authorityRepository.findByNameAndIdNot(authorityPatchRequest.getName(), uuid);

            checkAuthority(checkAuthorityByName);

            Authority authority = checkAuthority.get();

            authorityMapper.toAuthority(authorityPatchRequest, authority);

            Authority savedAuthority = authorityRepository.save(authority);

            return authorityMapper.toAuthorityResponse(savedAuthority);
        }

        throw new NotFoundException(ExceptionMessageCode.AUTHORITY_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND);
    }

    @Override
    public void deleteAuthority(String id) throws BaseException {
        UUID uuid = getAuthorityUUID(id);

        Optional<Authority> checkAuthority = authorityRepository.findById(uuid);

        if (checkAuthority.isPresent()) {
            try {
                checkAuthority.ifPresent(authority -> authorityRepository.deleteById(authority.getId()));

                return;
            }
            catch(DataIntegrityViolationException ex) {
                throw new BaseException(ExceptionMessageCode.AUTHORITY_CANNOT_DELETE, new String[]{id}, HttpStatus.CONFLICT, ex);
            }
        }

        throw new NotFoundException(ExceptionMessageCode.AUTHORITY_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND);
    }

    private UUID getAuthorityUUID(String id) throws NotFoundException {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new NotFoundException(ExceptionMessageCode.AUTHORITY_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND, ex);
        }
    }

    private void checkAuthority(Optional<Authority> checkAuthority) throws EntityConflictException {
        if (checkAuthority.isPresent()) {
            EntityConflictException ex = new EntityConflictException();

            ex.addError(ExceptionMessageCode.AUTHORITY_ALREADY_EXISTS_BY_NAME, new String[]{checkAuthority.get().getName()}, REQUEST_FIELD_NAME);

            throw ex;
        }
    }

}
