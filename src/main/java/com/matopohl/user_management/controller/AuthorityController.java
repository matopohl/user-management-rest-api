package com.matopohl.user_management.controller;

import com.matopohl.user_management.configuration.constants.ResponseMessageCode;
import com.matopohl.user_management.exception.custom.BaseException;
import com.matopohl.user_management.exception.custom.EntityConflictException;
import com.matopohl.user_management.exception.custom.NotFoundException;
import com.matopohl.user_management.model.request.AuthorityCreateRequest;
import com.matopohl.user_management.model.request.AuthorityPatchRequest;
import com.matopohl.user_management.model.request.AuthorityUpdateRequest;
import com.matopohl.user_management.model.response.AuthorityResponse;
import com.matopohl.user_management.model.response.base.BaseResponse;
import com.matopohl.user_management.service.AuthorityService;
import com.matopohl.user_management.service.helper.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Validated
@RequiredArgsConstructor
@RequestMapping(path = "authority")
@RestController
public class AuthorityController {

    private final AuthorityService authorityService;
    private final ResponseService responseService;

    @PreAuthorize("@userSecurityService.authorize(#request)")
    @GetMapping(path = {"", "/"}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<List<AuthorityResponse>>>> getAuthorities(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size, @RequestParam(required = false) String sort, HttpServletRequest request) {
        List<AuthorityResponse> authorityResponses = authorityService.getAuthorities(page, size, sort);

        for (AuthorityResponse authorityResponse : authorityResponses) {
            List<Link> authorityLinks = getAuthorityLinks(authorityResponse.getId().toString());
            authorityResponse.add(authorityLinks);
        }

        List<Link> links = List.of(linkTo(this.getClass()).withSelfRel());

        return responseService.createResponseEntity(authorityResponses, links, HttpStatus.OK, true);
    }

    @PreAuthorize("@userSecurityService.authorize(#request, '/authority/{id}')")
    @GetMapping(path = {"/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<AuthorityResponse>>> getAuthority(@PathVariable String id, HttpServletRequest request) throws NotFoundException {
        AuthorityResponse authorityResponse = authorityService.getAuthority(id);

        List<Link> authorityLinks = getAuthorityLinks(authorityResponse.getId().toString());

        return responseService.createResponseEntity(authorityResponse, authorityLinks, HttpStatus.OK, true);
    }

    @PreAuthorize("@userSecurityService.authorize(#request)")
    @PostMapping(path = {"", "/"}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<AuthorityResponse>>> createAuthority(@Valid @RequestBody AuthorityCreateRequest authorityCreateRequest, HttpServletRequest request) throws EntityConflictException {
        AuthorityResponse authorityResponse = authorityService.createAuthority(authorityCreateRequest);

        List<Link> authorityLinks = getAuthorityLinks(authorityResponse.getId().toString());

        return responseService.createResponseEntity(authorityResponse, authorityLinks, HttpStatus.CREATED, true);
    }

    @PreAuthorize("@userSecurityService.authorize(#request, '/authority/{id}')")
    @PutMapping(path = {"/{id}"}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<AuthorityResponse>>> updateAuthority(@Valid @RequestBody AuthorityUpdateRequest authorityUpdateRequest, @PathVariable String id, HttpServletRequest request) throws NotFoundException, EntityConflictException {
        AuthorityResponse authorityResponse = authorityService.updateAuthority(authorityUpdateRequest, id);

        List<Link> authorityLinks = getAuthorityLinks(authorityResponse.getId().toString());

        return responseService.createResponseEntity(authorityResponse, authorityLinks, HttpStatus.OK, true);
    }

    @PreAuthorize("@userSecurityService.authorize(#request, '/authority/{id}')")
    @PatchMapping(path = {"/{id}"}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<AuthorityResponse>>> patchAuthority(@Valid @RequestBody AuthorityPatchRequest authorityPatchRequest, @PathVariable String id, HttpServletRequest request) throws NotFoundException, EntityConflictException {
        AuthorityResponse authorityResponse = authorityService.patchAuthority(authorityPatchRequest, id);

        List<Link> authorityLinks = getAuthorityLinks(authorityResponse.getId().toString());

        return responseService.createResponseEntity(authorityResponse, authorityLinks, HttpStatus.OK, true);
    }

    @PreAuthorize("@userSecurityService.authorize(#request, '/authority/{id}')")
    @DeleteMapping(value = {"/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<String>>> deleteAuthority(@PathVariable String id, HttpServletRequest request, Locale locale) throws BaseException {
        authorityService.deleteAuthority(id);

        return responseService.createResponseEntity(ResponseMessageCode.AUTHORITY_DELETE_DELETE, new String[]{id}, null, HttpStatus.OK, true, locale);
    }

    List<Link> getAuthorityLinks(String id) {
        Link selfLink = linkTo(this.getClass()).slash(id).withSelfRel();

        List<Link> links = new ArrayList<>();
        links.add(selfLink);

        return links;
    }

}
