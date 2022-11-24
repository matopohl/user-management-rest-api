package com.matopohl.user_management.configuration.constants;

public class ExceptionMessageCode {

    public static final String REQUEST_MISSING_REQUEST_PART = "exception.request.missingRequestPart";
    public static final String REQUEST_METHOD_ARGUMENT_TYPE_MISMATCH = "exception.request.methodArgumentTypeMismatch";
    public static final String REQUEST_PROPERTY_REFERENCE = "exception.request.propertyReference";
    public static final String REQUEST_HTTP_MEDIA_TYPE_NOT_SUPPORTED = "exception.request.mediaType";
    public static final String REQUEST_VALIDATION_EXCEPTION = "exception.request.validation";

    public static final String PAGE_NOT_FOUND = "exception.page.notFound";
    public static final String PAGE_ACCESS_DENIED = "exception.page.accessDenied";
    public static final String PAGE_MUST_BE_LOGGED_IN = "exception.page.mustBeLoggedIn";

    public static final String JWT_TOKEN_UNSUPPORTED = "exception.jwtToken.unsupported";
    public static final String JW_TOKEN_MISSING = "exception.jwtToken.missing";
    public static final String REFRESH_TOKEN_NOT_FOUND = "exception.refreshToken.notFound";
    public static final String REFRESH_TOKEN_EXPIRED = "exception.refreshToken.expired";
    public static final String VERIFY_USER_TOKEN_NOT_FOUND = "exception.verifyUserToken.notFound";
    public static final String VERIFY_USER_TOKEN_EXPIRED ="exception.verifyUserToken.expired" ;
    public static final String RESET_USER_PASSWORD_TOKEN_NOT_FOUND = "exception.resetUserPasswordToken.notFound";
    public static final String RESET_USER_PASSWORD_TOKEN_EXPIRED = "exception.resetUserPasswordToken.expired";

    public static final String USER_UNVERIFIED = "exception.user.unverified";
    public static final String USER_UNVERIFIED_AND_EXPIRED = "exception.user.unverifiedAndExpired";
    public static final String USER_BAD_CREDENTIALS = "exception.user.badCredentials";
    public static final String USER_LOCK = "exception.user.lock";
    public static final String USER_BAN = "exception.user.ban";
    public static final String USER_NOT_FOUND = "exception.user.notFound";
    public static final String USER_ALREADY_EXISTS_BY_EMAIL = "exception.user.alreadyExistsByEmail";
    public static final String USER_ALREADY_EXISTS_BY_USERNAME = "exception.user.alreadyExistsByUsername";

    public static final String ROLE_NOT_FOUND = "exception.role.notFound";
    public static final String ROLE_ALREADY_EXISTS_BY_NAME = "exception.role.alreadyExistsByName";
    public static final String ROLE_CANNOT_DELETE = "exception.role.cannotDelete";

    public static final String AUTHORITY_NOT_FOUND = "exception.authority.notFound";
    public static final String AUTHORITY_ALREADY_EXISTS_BY_NAME = "exception.authority.alreadyExistsByName";
    public static final String AUTHORITY_CANNOT_DELETE = "exception.authority.cannotDelete";

    public static final String RESOURCE_NOT_FOUND = "exception.resource.notFound";
    public static final String RESOURCE_ALREADY_EXISTS_BY_URL_AND_METHOD = "exception.resource.alreadyExistsByUrlAndMethod";
    public static final String RESOURCE_CANNOT_DELETE = "exception.resource.cannotDelete";

    public static final String USER_DEVICE_NOT_FOUND = "exception.userDevice.notFound";

}
