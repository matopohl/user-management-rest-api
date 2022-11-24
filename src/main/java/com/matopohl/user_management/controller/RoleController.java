package com.matopohl.user_management.controller;

import com.matopohl.user_management.configuration.constants.ResponseMessageCode;
import com.matopohl.user_management.exception.custom.BaseException;
import com.matopohl.user_management.exception.custom.EntityConflictException;
import com.matopohl.user_management.exception.custom.NotFoundException;
import com.matopohl.user_management.model.request.RoleCreateRequest;
import com.matopohl.user_management.model.request.RolePatchRequest;
import com.matopohl.user_management.model.request.RoleUpdateRequest;
import com.matopohl.user_management.model.response.AuthorityResponse;
import com.matopohl.user_management.model.response.RoleResponse;
import com.matopohl.user_management.model.response.RoleRoleResponse;
import com.matopohl.user_management.model.response.base.BaseResponse;
import com.matopohl.user_management.service.RoleService;
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
@RequestMapping(path = "role")
@RestController
public class RoleController {

    private final RoleService roleService;
    private final ResponseService responseService;
    private final AuthorityController authorityController;

    @PreAuthorize("@userSecurityService.authorize(#request)")
    @GetMapping(path = {"", "/"}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<List<RoleResponse>>>> getRoles(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size, @RequestParam(required = false) String sort, HttpServletRequest request) {
        List<RoleResponse> roleResponses = roleService.getRoles(page, size, sort);

        for (RoleResponse roleResponse : roleResponses) {
            List<Link> roleLinks = getRoleLinks(roleResponse.getId().toString());
            roleResponse.add(roleLinks);

            setRoleRoleAndAuthorityLinks(roleResponse);
        }

        List<Link> links = List.of(linkTo(this.getClass()).withSelfRel());

        return responseService.createResponseEntity(roleResponses, links, HttpStatus.OK, true);
    }

    @PreAuthorize("@userSecurityService.authorize(#request, '/role/{id}')")
    @GetMapping(path = {"/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<RoleResponse>>> getRole(@PathVariable String id, HttpServletRequest request) throws NotFoundException {
        RoleResponse roleResponse = roleService.getRole(id);

        setRoleRoleAndAuthorityLinks(roleResponse);

        List<Link> roleLinks = getRoleLinks(roleResponse.getId().toString());

        return responseService.createResponseEntity(roleResponse, roleLinks, HttpStatus.OK, true);
    }

    @PreAuthorize("@userSecurityService.authorize(#request)")
    @PostMapping(path = {"", "/"}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<RoleResponse>>> createRole(@Valid @RequestBody RoleCreateRequest roleCreateRequest, HttpServletRequest request) throws EntityConflictException {
        RoleResponse roleResponse = roleService.createRole(roleCreateRequest);

        setRoleRoleAndAuthorityLinks(roleResponse);

        List<Link> roleLinks = getRoleLinks(roleResponse.getId().toString());

        return responseService.createResponseEntity(roleResponse, roleLinks, HttpStatus.CREATED, true);
    }

    @PreAuthorize("@userSecurityService.authorize(#request, '/role/{id}')")
    @PutMapping(path = {"/{id}"}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<RoleResponse>>> updateRole(@Valid @RequestBody RoleUpdateRequest roleUpdateRequest, @PathVariable String id, HttpServletRequest request) throws NotFoundException, EntityConflictException {
        RoleResponse roleResponse = roleService.updateRole(roleUpdateRequest, id);

        setRoleRoleAndAuthorityLinks(roleResponse);

        List<Link> roleLinks = getRoleLinks(roleResponse.getId().toString());

        return responseService.createResponseEntity(roleResponse, roleLinks, HttpStatus.OK, true);
    }

    @PreAuthorize("@userSecurityService.authorize(#request, '/role/{id}')")
    @PatchMapping(path = {"/{id}"}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<RoleResponse>>> patchRole(@Valid @RequestBody RolePatchRequest rolePatchRequest, @PathVariable String id, HttpServletRequest request) throws NotFoundException, EntityConflictException {
        RoleResponse roleResponse = roleService.patchRole(rolePatchRequest, id);

        setRoleRoleAndAuthorityLinks(roleResponse);

        List<Link> roleLinks = getRoleLinks(roleResponse.getId().toString());

        return responseService.createResponseEntity(roleResponse, roleLinks, HttpStatus.OK, true);
    }

    @PreAuthorize("@userSecurityService.authorize(#request, '/role/{id}')")
    @DeleteMapping(value = {"/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<String>>> deleteRole(@PathVariable String id, HttpServletRequest request, Locale locale) throws BaseException {
        roleService.deleteRole(id);

        return responseService.createResponseEntity(ResponseMessageCode.ROLE_DELETE_DELETE, new String[]{id}, null, HttpStatus.OK, true, locale);
    }

    List<Link> getRoleLinks(String id) {
        Link selfLink = linkTo(this.getClass()).slash(id).withSelfRel();

        List<Link> links = new ArrayList<>();
        links.add(selfLink);

        return links;
    }

    void setRoleRoleAndAuthorityLinks(RoleResponse roleResponse) {
        for(RoleRoleResponse parentRolesResponse : roleResponse.getParentRoles()) {
            List<Link> parentRolesLinks = getRoleLinks(parentRolesResponse.getId().toString());
            parentRolesResponse.add(parentRolesLinks);
        }

        for(RoleRoleResponse childrenRolesResponse : roleResponse.getChildrenRoles()) {
            List<Link> childrenRolesLinks = getRoleLinks(childrenRolesResponse.getId().toString());
            childrenRolesResponse.add(childrenRolesLinks);
        }

        for(AuthorityResponse authorityResponse : roleResponse.getAuthorities()) {
            List<Link> authorityLinks = getAuthorityLinks(authorityResponse.getId().toString());
            authorityResponse.add(authorityLinks);
        }
    }

    List<Link> getAuthorityLinks(String id) {
        return authorityController.getAuthorityLinks(id);
    }

}
