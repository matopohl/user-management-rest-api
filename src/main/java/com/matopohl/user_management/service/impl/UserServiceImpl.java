package com.matopohl.user_management.service.impl;

import com.matopohl.user_management.configuration.constants.ExceptionMessageCode;
import com.matopohl.user_management.domain.*;
import com.matopohl.user_management.event.publisher.MyEventPublisher;
import com.matopohl.user_management.exception.custom.BaseException;
import com.matopohl.user_management.exception.custom.EntityConflictException;
import com.matopohl.user_management.exception.custom.NotFoundException;
import com.matopohl.user_management.mapper.UserMapper;
import com.matopohl.user_management.model.request.UserPatchRequest;
import com.matopohl.user_management.model.request.UserRegisterRequest;
import com.matopohl.user_management.model.request.UserUpdateRequest;
import com.matopohl.user_management.model.response.UserResponse;
import com.matopohl.user_management.repository.AuthorityRepository;
import com.matopohl.user_management.repository.LoginAttemptLockCacheRepository;
import com.matopohl.user_management.repository.RoleRepository;
import com.matopohl.user_management.repository.UserRepository;
import com.matopohl.user_management.service.UserDeviceService;
import com.matopohl.user_management.service.UserService;
import com.matopohl.user_management.service.VerifyUserTokenService;
import com.matopohl.user_management.service.helper.FileService;
import com.matopohl.user_management.service.helper.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;
    private final RequestService requestService;
    private final UserDeviceService userDeviceService;
    private final VerifyUserTokenService verifyTokenService;
    private final FileService fileService;
    private final MyEventPublisher eventPublisher;
    private final LoginAttemptLockCacheRepository loginAttemptLockCacheRepository;

    @Value("${my.dir.user}")
    private String userDir;

    @Value("${my.cache.profile-images.dir}")
    private String cacheProfileImagesDir;

    public static final String ROBOHASH = "https://robohash.org/";
    public static final String ROBOHASH_EXTENSION = "png";
    private static final String REQUEST_FIELD_EMAIL = "email";
    private static final String REQUEST_FIELD_USERNAME = "username";
    private static final String REQUEST_FIELD_ROLES = "roles#";
    private static final String REQUEST_FIELD_AUTHORITIES = "authorities#";

    @Override
    public UserResponse registerUser(UserRegisterRequest userRegisterRequest, HttpServletRequest request) throws IOException, EntityConflictException {
        List<User> checkUsers = userRepository.findAllByEmailOrUsername(userRegisterRequest.getEmail(), userRegisterRequest.getUsername());

        Optional<User> userByEmail;

        userByEmail = checkUsers.stream().filter(r -> r.getEmail().equals(userRegisterRequest.getEmail())).findFirst();

        if (userByEmail.isPresent()) {
            eventPublisher.publishRegistrationFailUserExists(userByEmail.get());

            return userMapper.toUserResponse(userByEmail.get());
        }

        Optional<User> userByUsername;

        userByUsername = checkUsers.stream().filter(r -> r.getEmail().equals(userRegisterRequest.getUsername())).findFirst();

        if (userByUsername.isPresent()) {
            EntityConflictException entityConflictException = new EntityConflictException();

            entityConflictException.addError(ExceptionMessageCode.USER_ALREADY_EXISTS_BY_USERNAME, new String[]{userByUsername.get().getUsername()}, REQUEST_FIELD_USERNAME);

            throw entityConflictException;
        }

        User user = userMapper.toUser(userRegisterRequest, passwordEncoder);

        user.setUserDevices(Collections.singletonList(
                new UserDevice().setUserAgent(userDeviceService.getUserAgent(request))
                        .setName(userDeviceService.getDeviceDetails(request))
                        .setUser(user)
                )
        );

        boolean generateImage = userRegisterRequest.getProfileImage() == null || userRegisterRequest.getProfileImage().isEmpty();

        String tempFolder = userDir + "temp-" + UUID.randomUUID() + "/";

        if (generateImage) {
            byte[] profileImage = fileService.generateFile(tempFolder, cacheProfileImagesDir, ROBOHASH, ROBOHASH_EXTENSION, true);

            user.setProfileImage(new Document().setContent(profileImage));
        } else {
            user.setProfileImage(new Document().setContent(userRegisterRequest.getProfileImage().getBytes()));
        }

        VerifyUserToken verifyToken = verifyTokenService.createVerifyUserToken(user, true);

        User createdUser = userRepository.save(user);

        String userDirectory = userDir + createdUser.getId() + "/";

        if (generateImage) {
            Files.move(Paths.get(tempFolder), Paths.get(userDir + createdUser.getId().toString()));
        } else {
            fileService.saveFile(userDirectory, userRegisterRequest.getProfileImage().getBytes());
        }

        eventPublisher.publishRegistrationSuccessEvent(user, verifyToken);

        return userMapper.toUserResponse(createdUser);
    }

    @Override
    public List<UserResponse> getUsers(Integer page, Integer size, String sort) {
        Pageable pageable = requestService.getPageable(page, size, sort);

        Page<User> users = userRepository.findAll(pageable);

        return users.stream().map(userMapper::toUserResponse).collect(Collectors.toList());
    }

    @Override
    public UserResponse getUser(String id) throws NotFoundException {
        UUID uuid = getUserUUID(id);

        Optional<User> checkUser = userRepository.findById(uuid);

        if (checkUser.isPresent()) {
            return userMapper.toUserResponse(checkUser.get());
        }

        throw new NotFoundException(ExceptionMessageCode.USER_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND);
    }

    @Override
    public byte[] getProfileImage(String id) throws IOException, NotFoundException {
        UUID uuid = getUserUUID(id);

        Optional<User> checkUser = userRepository.findById(uuid);

        if (checkUser.isEmpty()) {
            throw new NotFoundException(ExceptionMessageCode.USER_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND);
        }

        try {
            Optional<Path> file = fileService.getActiveContentFromDir(userDir + id);

            if(file.isPresent()) {
                return Files.readAllBytes(file.get());
            }

            return getContentFromDatabase(uuid);
        } catch (Throwable ex) {
            return getContentFromDatabase(uuid);
        }
    }

    private byte[] getContentFromDatabase(UUID uuid) throws NotFoundException, IOException {
        Optional<User> checkUser = userRepository.findById(uuid);

        if (checkUser.isPresent()) {
            byte[] content = checkUser.get().getProfileImage().getContent();

            String userDirectory = userDir + checkUser.get().getId() + "/";

            fileService.saveFile(userDirectory, content);

            return content;
        }

        throw new NotFoundException(ExceptionMessageCode.USER_NOT_FOUND, new String[]{uuid.toString()}, HttpStatus.NOT_FOUND);
    }

    @Override
    public UserResponse updateUser(UserUpdateRequest userUpdateRequest, String id) throws NotFoundException, EntityConflictException, IOException {
        UUID uuid = getUserUUID(id);

        Optional<User> checkUser = userRepository.findById(uuid);

        if (checkUser.isPresent()) {
            EntityConflictException entityConflictException = new EntityConflictException();

            List<User> checkUserByEmailOrUsernameAndName = userRepository.findAllByEmailAndIdNotOrUsernameAndIdNot(userUpdateRequest.getEmail(), uuid, userUpdateRequest.getUsername(), uuid);

            checkUser(checkUserByEmailOrUsernameAndName, userUpdateRequest.getEmail(), userUpdateRequest.getUsername(), entityConflictException);

            User user = userMapper.toUser(userUpdateRequest, checkUser.get(), passwordEncoder, id);

            setRoles(user, userUpdateRequest.getRoles(), entityConflictException);
            setAuthorities(user, userUpdateRequest.getAuthorities(), entityConflictException);
            setProfileImage(user, userUpdateRequest.getProfileImage());

            if(entityConflictException.getErrors().size() > 0) {
                throw entityConflictException;
            }

            User savedUser = userRepository.save(user);

            return userMapper.toUserResponse(savedUser);
        }

        throw new NotFoundException(ExceptionMessageCode.USER_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND);
    }

    @Override
    public UserResponse patchUser(UserPatchRequest userPatchRequest, String id) throws EntityConflictException, NotFoundException, IOException {
        UUID uuid = getUserUUID(id);

        Optional<User> checkUser = userRepository.findById(uuid);

        if (checkUser.isPresent()) {
            EntityConflictException entityConflictException = new EntityConflictException();

            List<User> checkUserByEmailOrUsernameAndName = userRepository.findAllByEmailAndIdNotOrUsernameAndIdNot(userPatchRequest.getEmail(), uuid, userPatchRequest.getUsername(), uuid);

            checkUser(checkUserByEmailOrUsernameAndName, userPatchRequest.getEmail(), userPatchRequest.getUsername(), entityConflictException);

            User user = checkUser.get();

            userMapper.toUser(userPatchRequest, user, passwordEncoder);

            setRoles(user, userPatchRequest.getRoles(), entityConflictException);
            setAuthorities(user, userPatchRequest.getAuthorities(), entityConflictException);
            setProfileImage(user, userPatchRequest.getProfileImage());

            if(entityConflictException.getErrors().size() > 0) {
                throw entityConflictException;
            }

            User savedUser = userRepository.save(user);

            return userMapper.toUserResponse(savedUser);
        }

        throw new NotFoundException(ExceptionMessageCode.USER_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND);
    }



    @Override
    public void deactivateUser(String id) throws BaseException {
        UUID uuid = getUserUUID(id);

        Optional<User> checkUser = userRepository.findById(uuid);

        if (checkUser.isPresent()) {
            User user = checkUser.get();

            user.setActive(false);

            userRepository.save(user);

            return;
        }

        throw new NotFoundException(ExceptionMessageCode.USER_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND);
    }

    @Override
    public void unlockUser(String id) throws NotFoundException {
        UUID uuid = getUserUUID(id);

        Optional<User> checkUser = userRepository.findById(uuid);

        if (checkUser.isPresent()) {
            User user = checkUser.get();

            user.setLock(false);

            userRepository.save(user);

            loginAttemptLockCacheRepository.deleteByEmail(user.getEmail());

            return;
        }

        throw new NotFoundException(ExceptionMessageCode.USER_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND);
    }

    @Override
    public void banUser(String id) throws NotFoundException {
        UUID uuid = getUserUUID(id);

        Optional<User> checkUser = userRepository.findById(uuid);

        if (checkUser.isPresent()) {
            User user = checkUser.get();

            user.setBan(true);

            userRepository.save(user);

            eventPublisher.publishBanEvent(user);

            return;
        }

        throw new NotFoundException(ExceptionMessageCode.USER_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND);
    }

    @Override
    public void unbanUser(String id) throws NotFoundException {
        UUID uuid = getUserUUID(id);

        Optional<User> checkUser = userRepository.findById(uuid);

        if (checkUser.isPresent()) {
            User user = checkUser.get();

            user.setBan(false);

            userRepository.save(user);

            eventPublisher.publishUnbanEvent(user);

            return;
        }

        throw new NotFoundException(ExceptionMessageCode.USER_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND);
    }

    private UUID getUserUUID(String id) throws NotFoundException {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new NotFoundException(ExceptionMessageCode.USER_NOT_FOUND, new String[]{id}, HttpStatus.NOT_FOUND, ex);
        }
    }

    private void checkUser(List<User> checkUsers, String email, String username, EntityConflictException ex) {
        Optional<User> userByEmail;

        userByEmail = checkUsers.stream().filter(r -> r.getEmail().equals(email)).findFirst();

        if (userByEmail.isPresent()) {
            ex.addError(ExceptionMessageCode.USER_ALREADY_EXISTS_BY_EMAIL, new String[]{email}, REQUEST_FIELD_EMAIL);
        }

        Optional<User> userByUsername;

        userByUsername = checkUsers.stream().filter(r -> r.getEmail().equals(username)).findFirst();

        if (userByUsername.isPresent()) {
            ex.addError(ExceptionMessageCode.USER_ALREADY_EXISTS_BY_USERNAME, new String[]{username}, REQUEST_FIELD_USERNAME);
        }
    }

    private void setRoles(User user, List<String> roles, EntityConflictException entityConflictException) {
        if(roles != null) {
            for(int i = 0; i < roles.size(); i++) {
                String roleId = roles.get(i);

                UUID roleUuid;

                try {
                    roleUuid = UUID.fromString(roleId);
                } catch (IllegalArgumentException ex) {
                    entityConflictException.addError(ExceptionMessageCode.ROLE_NOT_FOUND, new String[]{roleId}, REQUEST_FIELD_ROLES + i);

                    continue;
                }

                Optional<Role> childrenRole = roleRepository.findById(roleUuid);

                if (childrenRole.isPresent()) {
                    user.getRoles().add(childrenRole.get());
                } else {
                    entityConflictException.addError(ExceptionMessageCode.ROLE_NOT_FOUND, new String[]{roleId}, REQUEST_FIELD_ROLES + i);
                }
            }
        }
    }

    private void setAuthorities(User user, List<String> authorities, EntityConflictException entityConflictException) {
        if (authorities != null) {
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
                    user.getUserAuthorities().add(authority.get());
                } else {
                    entityConflictException.addError(ExceptionMessageCode.AUTHORITY_NOT_FOUND, new String[]{authorityId}, REQUEST_FIELD_AUTHORITIES + i);
                }
            }
        }
    }

    private void setProfileImage(User user, MultipartFile profileImage) throws IOException {
        if(profileImage != null && !profileImage.isEmpty()) {
            user.setProfileImage(new Document().setContent(profileImage.getBytes()));

            String userDirectory = userDir + user.getId() + "/";

            fileService.saveFile(userDirectory, profileImage.getBytes());
        }
    }

}
