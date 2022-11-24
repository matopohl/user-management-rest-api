package com.matopohl.user_management.controller;

import com.matopohl.user_management.configuration.constants.ResponseMessageCode;
import com.matopohl.user_management.exception.custom.BaseException;
import com.matopohl.user_management.exception.custom.NotFoundException;
import com.matopohl.user_management.model.request.*;
import com.matopohl.user_management.model.response.RefreshTokenResponse;
import com.matopohl.user_management.model.response.RoleResponse;
import com.matopohl.user_management.model.response.UserLoginResponse;
import com.matopohl.user_management.model.response.UserResponse;
import com.matopohl.user_management.model.response.base.BaseResponse;
import com.matopohl.user_management.service.AuthenticationService;
import com.matopohl.user_management.service.helper.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Locale;

@Validated
@RequiredArgsConstructor
@RequestMapping(path = {"", "/"})
@RestController
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final ResponseService responseService;
    private final UserController userController;

    @PostMapping(path = {"/login"}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<UserLoginResponse>>> loginUser(@Valid @RequestBody UserLoginRequest userloginRequest, HttpServletRequest request, HttpServletResponse response) throws Exception {
        UserLoginResponse loginResponse = authenticationService.loginUser(userloginRequest, request, response);

        List<Link> userLinks = getUserLinks(loginResponse.getUser().getId().toString(), request);

        for (RoleResponse roleResponse : loginResponse.getUser().getRoles()) {
            List<Link> roleLinks = getRoleLinks(roleResponse.getId().toString());
            roleResponse.add(roleLinks);

            setRoleRoleAndAuthorityLinks(roleResponse);
        }

        setFlatRoleAndFlatAuthorityAndAuthorityLinks(loginResponse.getUser());

        loginResponse.getUser().add(userLinks);

        return responseService.createResponseEntity(loginResponse, null, HttpStatus.OK, true);
    }

    @PostMapping(path = {"/refresh-token/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<RefreshTokenResponse>>> refreshToken(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) throws NotFoundException, IOException, NoSuchMethodException, InvalidKeySpecException, NoSuchAlgorithmException {
        RefreshTokenResponse refreshTokenResponse = authenticationService.refreshToken(id, request, response);

        return responseService.createResponseEntity(refreshTokenResponse, null, HttpStatus.OK, true);
    }

    @PostMapping(path = {"/verify-user-token/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<String>>> verifyUserToken(@PathVariable String id, Locale locale) throws BaseException {
        authenticationService.verifyUserToken(id);

        return responseService.createResponseEntity(ResponseMessageCode.USER_VERIFY_TOKEN_POST, new String[]{id}, null, HttpStatus.OK, true, locale);
    }

    @PostMapping(path = {"reset-user-password"}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<String>>> resetUserPassword(@Valid @RequestBody ResetUserPasswordRequest userResetPasswordRequest, HttpServletRequest request, HttpServletResponse response, Locale locale) {
        authenticationService.resetUserPassword(userResetPasswordRequest, request, response);

        return responseService.createResponseEntity(ResponseMessageCode.USER_RESET_PASSWORD_POST, new String[]{userResetPasswordRequest.getEmail()}, null, HttpStatus.ACCEPTED, true, locale);
    }

    @PostMapping(path = {"/reset-user-password-token/{id}"}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<String>>> resetUserPasswordToken(@Valid @RequestBody ResetUserPasswordTokenRequest userResetPasswordTokenRequest, @PathVariable String id, Locale locale) throws BaseException {
        authenticationService.resetUserPasswordToken(userResetPasswordTokenRequest, id);

        return responseService.createResponseEntity(ResponseMessageCode.USER_RESET_PASSWORD_TOKEN_POST, new String[]{id}, null, HttpStatus.OK, true, locale);
    }

    @DeleteMapping(value = {"/logout/{id}"}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<RepresentationModel<BaseResponse<String>>> logout(@PathVariable String id, HttpServletRequest request, Locale locale) throws NotFoundException {
        authenticationService.logoutUser(id, request);

        return responseService.createResponseEntity(ResponseMessageCode.USER_LOGOUT_DELETE, null, null, HttpStatus.OK, true, locale);
    }

    List<Link> getUserLinks(String id, HttpServletRequest request) throws NoSuchMethodException {
        return userController.getUserLinks(id, request);
    }

    List<Link> getRoleLinks(String id) {
        return userController.getRoleLinks(id);
    }

    void setRoleRoleAndAuthorityLinks(RoleResponse roleResponse) {
        userController.setRoleRoleAndAuthorityLinks(roleResponse);
    }

    void setFlatRoleAndFlatAuthorityAndAuthorityLinks(UserResponse userResponse) {
        userController.setFlatRoleAndFlatAuthorityAndAuthorityLinks(userResponse);
    }

}
