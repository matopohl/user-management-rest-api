package com.matopohl.user_management.controller;

import com.matopohl.user_management.configuration.constants.ResponseMessageCode;
import com.matopohl.user_management.exception.custom.BaseException;
import com.matopohl.user_management.exception.custom.EntityConflictException;
import com.matopohl.user_management.exception.custom.NotFoundException;
import com.matopohl.user_management.model.request.ResourceCreateRequest;
import com.matopohl.user_management.model.request.ResourcePatchRequest;
import com.matopohl.user_management.model.request.ResourceUpdateRequest;
import com.matopohl.user_management.model.response.AuthorityResponse;
import com.matopohl.user_management.model.response.ResourceResponse;
import com.matopohl.user_management.model.response.base.BaseResponse;
import com.matopohl.user_management.service.ResourceService;
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
@RequestMapping(path = "resource")
@RestController
public class ResourceController {

    private final ResourceService resourceService;
    private final ResponseService responseService;
    private final AuthorityController authorityController;

    @PreAuthorize("@userSecurityService.authorize(#request)")
    @GetMapping(path = {"", "/"}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<List<ResourceResponse>>>> getResources(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size, @RequestParam(required = false) String sort, HttpServletRequest request) {
        List<ResourceResponse> resourceResponses = resourceService.getResources(page, size, sort);

        for (ResourceResponse resourceResponse : resourceResponses) {
            List<Link> resourceLinks = getResourceLinks(resourceResponse.getId().toString());
            resourceResponse.add(resourceLinks);

            setAuthorityLinks(resourceResponse);
        }

        List<Link> links = List.of(linkTo(this.getClass()).withSelfRel());

        return responseService.createResponseEntity(resourceResponses, links, HttpStatus.OK, true);
    }

    @PreAuthorize("@userSecurityService.authorize(#request, '/resource/{id}')")
    @GetMapping(path = {"/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<ResourceResponse>>> getResource(@PathVariable String id, HttpServletRequest request) throws NotFoundException {
        ResourceResponse resourceResponse = resourceService.getResource(id);

        setAuthorityLinks(resourceResponse);

        List<Link> resourceLinks = getResourceLinks(resourceResponse.getId().toString());

        return responseService.createResponseEntity(resourceResponse, resourceLinks, HttpStatus.OK, true);
    }

    @PreAuthorize("@userSecurityService.authorize(#request)")
    @PostMapping(path = {"", "/"}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<ResourceResponse>>> createResource(@Valid @RequestBody ResourceCreateRequest resourceCreateRequest, HttpServletRequest request) throws EntityConflictException, NotFoundException {
        ResourceResponse resourceResponse = resourceService.createResource(resourceCreateRequest);

        setAuthorityLinks(resourceResponse);

        List<Link> resourceLinks = getResourceLinks(resourceResponse.getId().toString());

        return responseService.createResponseEntity(resourceResponse, resourceLinks, HttpStatus.CREATED, true);
    }

    @PreAuthorize("@userSecurityService.authorize(#request, '/resource/{id}')")
    @PutMapping(path = {"/{id}"}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<ResourceResponse>>> updateAuthority(@Valid @RequestBody ResourceUpdateRequest resourceUpdateRequest, @PathVariable String id, HttpServletRequest request) throws NotFoundException, EntityConflictException {
        ResourceResponse resourceResponse = resourceService.updateResource(resourceUpdateRequest, id);

        setAuthorityLinks(resourceResponse);

        List<Link> resourceLinks = getResourceLinks(resourceResponse.getId().toString());

        return responseService.createResponseEntity(resourceResponse, resourceLinks, HttpStatus.OK, true);
    }

    @PreAuthorize("@userSecurityService.authorize(#request, '/resource/{id}')")
    @PatchMapping(path = {"/{id}"}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<ResourceResponse>>> patchAuthority(@Valid @RequestBody ResourcePatchRequest resourcePatchRequest, @PathVariable String id, HttpServletRequest request) throws NotFoundException, EntityConflictException {
        ResourceResponse resourceResponse = resourceService.patchResource(resourcePatchRequest, id);

        setAuthorityLinks(resourceResponse);

        List<Link> resourceLinks = getResourceLinks(resourceResponse.getId().toString());

        return responseService.createResponseEntity(resourceResponse, resourceLinks, HttpStatus.OK, true);
    }

    @PreAuthorize("@userSecurityService.authorize(#request, '/resource/{id}')")
    @DeleteMapping(value = {"/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<String>>> deleteResource(@PathVariable String id, HttpServletRequest request, Locale locale) throws BaseException {
        resourceService.deleteResource(id);

        return responseService.createResponseEntity(ResponseMessageCode.RESOURCE_DELETE_DELETE, new String[]{id}, null, HttpStatus.OK, true, locale);
    }

    List<Link> getResourceLinks(String id) {
        Link selfLink = linkTo(this.getClass()).slash(id).withSelfRel();

        List<Link> links = new ArrayList<>();
        links.add(selfLink);

        return links;
    }

    void setAuthorityLinks(ResourceResponse resourceResponse) {
        for(AuthorityResponse authorityResponse : resourceResponse.getAuthorities()) {
            List<Link> authorityLinks = getAuthorityLinks(authorityResponse.getId().toString());
            authorityResponse.add(authorityLinks);
        }
    }

    List<Link> getAuthorityLinks(String id) {
        return authorityController.getAuthorityLinks(id);
    }

}
