package com.matopohl.user_management.controller;

import com.matopohl.user_management.configuration.constants.ResponseMessageCode;
import com.matopohl.user_management.exception.custom.BaseException;
import com.matopohl.user_management.exception.custom.EntityConflictException;
import com.matopohl.user_management.exception.custom.NotFoundException;
import com.matopohl.user_management.model.request.UserPatchRequest;
import com.matopohl.user_management.model.request.UserRegisterRequest;
import com.matopohl.user_management.model.request.UserUpdateRequest;
import com.matopohl.user_management.model.response.AuthorityResponse;
import com.matopohl.user_management.model.response.RoleResponse;
import com.matopohl.user_management.model.response.RoleRoleResponse;
import com.matopohl.user_management.model.response.UserResponse;
import com.matopohl.user_management.model.response.base.BaseResponse;
import com.matopohl.user_management.service.UserService;
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
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Validated
@RequiredArgsConstructor
@RequestMapping(path = "user")
@RestController
public class UserController {

    private final UserService userService;
    private final Validator validator;
    private final ResponseService responseService;
    private final RoleController roleController;
    private final AuthorityController authorityController;

    @PreAuthorize("@userSecurityService.authorize(#request)")
    @GetMapping(path = {"", "/"}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<List<UserResponse>>>> getUsers(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size, @RequestParam(required = false) String sort, HttpServletRequest request) throws NoSuchMethodException {
        List<UserResponse> userResponses = userService.getUsers(page, size, sort);

        for (UserResponse userResponse : userResponses) {
            List<Link> userLinks = getUserLinks(userResponse.getId().toString(), request);
            userResponse.add(userLinks);

            setAllLinks(userResponse);
        }

        List<Link> links = List.of(linkTo(this.getClass()).withSelfRel());

        return responseService.createResponseEntity(userResponses, links, HttpStatus.OK, true);
    }

    @PreAuthorize("@userSecurityService.isUser(#id) || @userSecurityService.authorize(#request, '/user/{id}')")
    @GetMapping(path = {"/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<UserResponse>>> getUser(@PathVariable String id, HttpServletRequest request) throws NotFoundException, NoSuchMethodException {
        UserResponse userResponse = userService.getUser(id);

        setAllLinks(userResponse);

        List<Link> userLinks = getUserLinks(userResponse.getId().toString(), request);

        return responseService.createResponseEntity(userResponse, userLinks, HttpStatus.OK, true);
    }

    @PreAuthorize("@userSecurityService.isUser(#id) || @userSecurityService.authorize(#request, '/user/{id}/profile-image')")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = {"/{id}/profile-image"}, produces = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public byte[] getProfileImage(@PathVariable String id, HttpServletRequest request) throws IOException, NotFoundException {
        return userService.getProfileImage(id);
    }

    @PostMapping(path = {"", "/"}, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<String>>> registerUser(@RequestPart(name = "user") UserRegisterRequest userRequest, @RequestPart(required = false) MultipartFile profileImage, HttpServletRequest request, Locale locale) throws IOException, InterruptedException, ExecutionException, NoSuchMethodException, EntityConflictException {
        userRequest.setProfileImage(profileImage);

        Set<ConstraintViolation<UserRegisterRequest>> violations = validator.validate(userRequest);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        UserResponse userResponse = userService.registerUser(userRequest, request);

        List<Link> userLinks = getUserLinks(userResponse.getId().toString(), request);

        return responseService.createResponseEntity(ResponseMessageCode.USER_REGISTER_POST, new String[]{userRequest.getEmail()}, userLinks, HttpStatus.ACCEPTED, true, locale);
    }

    @PreAuthorize("@userSecurityService.isUser(#id) || @userSecurityService.authorize(#request, '/user/{id}')")
    @PutMapping(path = {"/{id}"}, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<UserResponse>>> updateUser(@RequestPart(name = "user") UserUpdateRequest userUpdateRequest, @RequestPart(required = false) MultipartFile profileImage, @PathVariable String id, HttpServletRequest request) throws NotFoundException, EntityConflictException, IOException, NoSuchMethodException {
        userUpdateRequest.setProfileImage(profileImage);

        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(userUpdateRequest);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        UserResponse userResponse = userService.updateUser(userUpdateRequest, id);

        setAllLinks(userResponse);

        List<Link> userLinks = getUserLinks(userResponse.getId().toString(), request);

        return responseService.createResponseEntity(userResponse, userLinks, HttpStatus.OK, true);
    }

    @PreAuthorize("@userSecurityService.isUser(#id) || @userSecurityService.authorize(#request, '/user/{id}')")
    @PatchMapping(path = {"/{id}"}, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<UserResponse>>> patchUser(@RequestPart(name = "user") UserPatchRequest userPatchRequest, @RequestPart(required = false) MultipartFile profileImage, @PathVariable String id, HttpServletRequest request) throws NotFoundException, EntityConflictException, IOException, NoSuchMethodException {
        userPatchRequest.setProfileImage(profileImage);

        Set<ConstraintViolation<UserPatchRequest>> violations = validator.validate(userPatchRequest);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        UserResponse userResponse = userService.patchUser(userPatchRequest, id);

        setAllLinks(userResponse);

        List<Link> userLinks = getUserLinks(userResponse.getId().toString(), request);

        return responseService.createResponseEntity(userResponse, userLinks, HttpStatus.OK, true);
    }

    @PreAuthorize("@userSecurityService.isUser(#id) || @userSecurityService.authorize(#request, '/user/{id}/deactivate')")
    @DeleteMapping(path = {"/{id}/deactivate"}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<String>>> deactivateUser(@PathVariable String id, HttpServletRequest request, Locale locale) throws BaseException {
        userService.deactivateUser(id);

        return responseService.createResponseEntity(ResponseMessageCode.USER_DEACTIVATE_DELETE, new String[]{id}, null, HttpStatus.OK, true, locale);
    }

    @PreAuthorize("@userSecurityService.authorize(#request, '/user/{id}/unlock')")
    @PostMapping(path = {"/{id}/unlock"}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<String>>> unlockUser(@PathVariable String id, HttpServletRequest request, Locale locale) throws BaseException {
        userService.unlockUser(id);

        return responseService.createResponseEntity(ResponseMessageCode.USER_UNLOCK_POST, new String[]{id}, null, HttpStatus.OK, true, locale);
    }

    @PreAuthorize("@userSecurityService.authorize(#request, '/user/{id}/ban')")
    @PostMapping(path = {"/{id}/ban"}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<String>>> banUser(@PathVariable String id, HttpServletRequest request, Locale locale) throws BaseException {
        userService.banUser(id);

        return responseService.createResponseEntity(ResponseMessageCode.USER_BAN_POST, new String[]{id}, null, HttpStatus.OK, true, locale);
    }

    @PreAuthorize("@userSecurityService.authorize(#request, '/user/{id}/unban')")
    @PostMapping(path = {"/{id}/unban"}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<String>>> unbanUser(@PathVariable String id, HttpServletRequest request, Locale locale) throws BaseException {
        userService.unbanUser(id);

        return responseService.createResponseEntity(ResponseMessageCode.USER_UNBAN_POST, new String[]{id}, null, HttpStatus.OK, true, locale);
    }

    List<Link> getUserLinks(String id, HttpServletRequest request) throws NoSuchMethodException {
        Link selfLink = linkTo(this.getClass()).slash(id).withSelfRel();

        Method method = UserController.class.getMethod("getProfileImage", String.class, HttpServletRequest.class);
        Link profileImageLink = linkTo(method, id, request).withRel("profile image");

        List<Link> links = new ArrayList<>();
        links.add(selfLink);
        links.add(profileImageLink);

        return links;
    }

    List<Link> getRoleLinks(String id) {
        return roleController.getRoleLinks(id);
    }

    List<Link> getAuthorityLinks(String id) {
        return authorityController.getAuthorityLinks(id);
    }

    void setRoleRoleAndAuthorityLinks(RoleResponse roleResponse) {
        roleController.setRoleRoleAndAuthorityLinks(roleResponse);
    }

    void setFlatRoleAndFlatAuthorityAndAuthorityLinks(UserResponse userResponse) {
        for (AuthorityResponse authorityResponse : userResponse.getAuthorities()) {
            List<Link> rolesLinks = getAuthorityLinks(authorityResponse.getId().toString());
            authorityResponse.add(rolesLinks);
        }

        for (RoleRoleResponse flatRoleResponse : userResponse.getFlatRoles()) {
            List<Link> rolesLinks = getRoleLinks(flatRoleResponse.getId().toString());
            flatRoleResponse.add(rolesLinks);
        }

        for (AuthorityResponse flatAuthorityResponse : userResponse.getFlatAuthorities()) {
            List<Link> rolesLinks = getAuthorityLinks(flatAuthorityResponse.getId().toString());
            flatAuthorityResponse.add(rolesLinks);
        }
    }

    void setAllLinks(UserResponse userResponse) {
        for (RoleResponse roleResponse : userResponse.getRoles()) {
            List<Link> roleLinks = getRoleLinks(roleResponse.getId().toString());
            roleResponse.add(roleLinks);

            setRoleRoleAndAuthorityLinks(roleResponse);
        }

        setFlatRoleAndFlatAuthorityAndAuthorityLinks(userResponse);
    }

}
