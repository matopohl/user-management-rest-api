package com.matopohl.user_management.configuration.db;

import com.matopohl.user_management.domain.*;
import com.matopohl.user_management.repository.ResourceRepository;
import com.matopohl.user_management.repository.UserRepository;
import com.matopohl.user_management.service.helper.FileService;
import com.matopohl.user_management.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Configuration
class LoadDatabase {

    @Value("${my.dir.user}")
    private String userDir;

    @Value("${my.cache.profile-images.dir}")
    private String cacheProfileImagesDir;

    @Bean
    CommandLineRunner loadDatabaseRunner(UserRepository userRepository, ResourceRepository resourceRepository, PasswordEncoder passwordEncoder, FileService fileService) {
        return args -> {
            if(userRepository.count() == 0) {
                // Authorities

                Authority authorityAuthorityRead = new Authority().setName("authority:read");
                Authority authorityAuthorityCreate = new Authority().setName("authority:create");
                Authority authorityAuthorityUpdate = new Authority().setName("authority:update");
                Authority authorityAuthorityPatch = new Authority().setName("authority:patch");
                Authority authorityAuthorityDelete = new Authority().setName("authority:delete");



                Authority authorityRoleRead = new Authority().setName("role:read");
                Authority authorityRoleCreate = new Authority().setName("role:create");
                Authority authorityRoleUpdate = new Authority().setName("role:update");
                Authority authorityRolePatch = new Authority().setName("role:patch");
                Authority authorityRoleDelete = new Authority().setName("role:delete");



                Authority authorityResourceRead = new Authority().setName("resource:read");
                Authority authorityResourceCreate = new Authority().setName("resource:create");
                Authority authorityResourceUpdate = new Authority().setName("resource:update");
                Authority authorityResourcePatch = new Authority().setName("resource:patch");
                Authority authorityResourceDelete = new Authority().setName("resource:delete");



                Authority authorityUserRead = new Authority().setName("user:read");
                Authority authorityUserProfileImageRead = new Authority().setName("userProfileImage:read");
                Authority authorityUserUpdate = new Authority().setName("user:update");
                Authority authorityUserPatch = new Authority().setName("user:patch");
                Authority authorityUserDeactivate = new Authority().setName("user:deactivate");
                Authority authorityUserUnlock = new Authority().setName("user:unlock");
                Authority authorityUserBan = new Authority().setName("user:ban");
                Authority authorityUserUnban = new Authority().setName("user:unban");



                // Roles

                Role roleRead = new Role().setName("read");
                Role roleAdmin = new Role().setName("admin");



                roleRead.getRoleAuthorities().add(authorityAuthorityRead);
                roleRead.getRoleAuthorities().add(authorityRoleRead);
                roleRead.getRoleAuthorities().add(authorityResourceRead);
                roleRead.getRoleAuthorities().add(authorityUserRead);
                roleRead.getRoleAuthorities().add(authorityUserProfileImageRead);



                roleAdmin.getRoleAuthorities().add(authorityAuthorityCreate);
                roleAdmin.getRoleAuthorities().add(authorityAuthorityUpdate);
                roleAdmin.getRoleAuthorities().add(authorityAuthorityPatch);
                roleAdmin.getRoleAuthorities().add(authorityAuthorityDelete);

                roleAdmin.getRoleAuthorities().add(authorityRoleCreate);
                roleAdmin.getRoleAuthorities().add(authorityRoleUpdate);
                roleAdmin.getRoleAuthorities().add(authorityRolePatch);
                roleAdmin.getRoleAuthorities().add(authorityRoleDelete);

                roleAdmin.getRoleAuthorities().add(authorityResourceCreate);
                roleAdmin.getRoleAuthorities().add(authorityResourceUpdate);
                roleAdmin.getRoleAuthorities().add(authorityResourcePatch);
                roleAdmin.getRoleAuthorities().add(authorityResourceDelete);

                roleAdmin.getRoleAuthorities().add(authorityUserUpdate);
                roleAdmin.getRoleAuthorities().add(authorityUserPatch);
                roleAdmin.getRoleAuthorities().add(authorityUserDeactivate);
                roleAdmin.getRoleAuthorities().add(authorityUserUnlock);
                roleAdmin.getRoleAuthorities().add(authorityUserBan);
                roleAdmin.getRoleAuthorities().add(authorityUserUnban);



                roleRead.getParentRoles().add(roleAdmin);
                roleAdmin.getChildrenRoles().add(roleRead);



                // Users

                String tempFolderAdmin = userDir + "temp-" + UUID.randomUUID() + "/";

                User userAdmin = new User()
                        .setFirstName("Martin")
                        .setLastName("Pohl")
                        .setEmail("admin@test.com")
                        .setPassword(passwordEncoder.encode("asdasdasd"))
                        .setVerified(true)
                        .setActive(true)
                        .setLock(false)
                        .setBan(false)
                        .setCreationDate(ZonedDateTime.now(ZoneOffset.UTC))
                        .setProfileImage(new Document().setContent(fileService.generateFile(tempFolderAdmin, cacheProfileImagesDir, UserServiceImpl.ROBOHASH, UserServiceImpl.ROBOHASH_EXTENSION, false)));

                userAdmin.getRoles().add(roleAdmin);



                String tempFolderReader = userDir + "temp-" + UUID.randomUUID() + "/";

                User userReader = new User()
                        .setFirstName("Jan")
                        .setLastName("Novák")
                        .setEmail("reader@test.com")
                        .setPassword(passwordEncoder.encode("asdasdasd"))
                        .setVerified(true)
                        .setActive(true)
                        .setLock(false)
                        .setBan(false)
                        .setCreationDate(ZonedDateTime.now(ZoneOffset.UTC))
                        .setProfileImage(new Document().setContent(fileService.generateFile(tempFolderReader, cacheProfileImagesDir, UserServiceImpl.ROBOHASH, UserServiceImpl.ROBOHASH_EXTENSION, false)));

                userReader.getRoles().add(roleRead);



                String tempFolderUserNoAuth = userDir + "temp-" + UUID.randomUUID() + "/";

                User userNoAuth = new User()
                        .setFirstName("Zdeněk")
                        .setLastName("Pešek")
                        .setEmail("user@test.com")
                        .setPassword(passwordEncoder.encode("asdasdasd"))
                        .setVerified(true)
                        .setActive(true)
                        .setLock(false)
                        .setBan(false)
                        .setCreationDate(ZonedDateTime.now(ZoneOffset.UTC))
                        .setProfileImage(new Document().setContent(fileService.generateFile(tempFolderUserNoAuth, cacheProfileImagesDir, UserServiceImpl.ROBOHASH, UserServiceImpl.ROBOHASH_EXTENSION, true)));



                List<User> users = List.of(
                        userAdmin,
                        userReader,
                        userNoAuth
                );

                List<String> tempFolders = List.of(
                        tempFolderAdmin,
                        tempFolderReader,
                        tempFolderUserNoAuth
                );


                List<User> savedUsers = userRepository.saveAll(users);

                for(int i = 0; i < savedUsers.size(); i++) {
                    Files.move(Paths.get(tempFolders.get(i)), Paths.get(userDir + savedUsers.get(i).getId().toString()));
                }



                // Resources

                Resource resourceAuthoritiesGet = new Resource().setRequestUrl("/authority").setRequestMethod("GET");
                resourceAuthoritiesGet.getResourceAuthorities().add(authorityAuthorityRead);

                Resource resourceAuthorityGet = new Resource().setRequestUrl("/authority/{id}").setRequestMethod("GET");
                resourceAuthorityGet.getResourceAuthorities().add(authorityAuthorityRead);

                Resource resourceAuthorityCreate = new Resource().setRequestUrl("/authority").setRequestMethod("POST");
                resourceAuthorityCreate.getResourceAuthorities().add(authorityAuthorityCreate);

                Resource resourceAuthorityUpdate = new Resource().setRequestUrl("/authority/{id}").setRequestMethod("PUT");
                resourceAuthorityUpdate.getResourceAuthorities().add(authorityAuthorityUpdate);

                Resource resourceAuthorityPatch = new Resource().setRequestUrl("/authority/{id}").setRequestMethod("PATCH");
                resourceAuthorityPatch.getResourceAuthorities().add(authorityAuthorityPatch);

                Resource resourceAuthorityDelete = new Resource().setRequestUrl("/authority/{id}").setRequestMethod("DELETE");
                resourceAuthorityDelete.getResourceAuthorities().add(authorityAuthorityDelete);



                Resource resourceRolesGet = new Resource().setRequestUrl("/role").setRequestMethod("GET");
                resourceRolesGet.getResourceAuthorities().add(authorityRoleRead);

                Resource resourceRoleGet = new Resource().setRequestUrl("/role/{id}").setRequestMethod("GET");
                resourceRoleGet.getResourceAuthorities().add(authorityRoleRead);

                Resource resourceRoleCreate = new Resource().setRequestUrl("/role").setRequestMethod("POST");
                resourceRoleCreate.getResourceAuthorities().add(authorityRoleCreate);

                Resource resourceRoleUpdate = new Resource().setRequestUrl("/role/{id}").setRequestMethod("PUT");
                resourceRoleUpdate.getResourceAuthorities().add(authorityRoleUpdate);

                Resource resourceRolePatch = new Resource().setRequestUrl("/role/{id}").setRequestMethod("PATCH");
                resourceRolePatch.getResourceAuthorities().add(authorityRolePatch);

                Resource resourceRoleDelete = new Resource().setRequestUrl("/role/{id}").setRequestMethod("DELETE");
                resourceRoleDelete.getResourceAuthorities().add(authorityRoleDelete);



                Resource resourceUsersGet = new Resource().setRequestUrl("/user").setRequestMethod("GET");
                resourceUsersGet.getResourceAuthorities().add(authorityUserRead);

                Resource resourceUserGet = new Resource().setRequestUrl("/user/{id}").setRequestMethod("GET");
                resourceUserGet.getResourceAuthorities().add(authorityUserRead);

                Resource resourceUserProfileImageGet = new Resource().setRequestUrl("/user/{id}/profile-image").setRequestMethod("GET");
                resourceUserProfileImageGet.getResourceAuthorities().add(authorityUserProfileImageRead);

                Resource resourceUserUpdate = new Resource().setRequestUrl("/user/{id}").setRequestMethod("PUT");
                resourceUserUpdate.getResourceAuthorities().add(authorityUserUpdate);

                Resource resourceUserPatch = new Resource().setRequestUrl("/user/{id}").setRequestMethod("PATCH");
                resourceUserPatch.getResourceAuthorities().add(authorityUserPatch);

                Resource resourceUserDeactivate = new Resource().setRequestUrl("/user/{id}/deactivate").setRequestMethod("DELETE");
                resourceUserDeactivate.getResourceAuthorities().add(authorityUserDeactivate);

                Resource resourceUserUnlock = new Resource().setRequestUrl("/user/{id}/unlock").setRequestMethod("POST");
                resourceUserUnlock.getResourceAuthorities().add(authorityUserUnlock);

                Resource resourceUserBan = new Resource().setRequestUrl("/user/{id}/ban").setRequestMethod("POST");
                resourceUserBan.getResourceAuthorities().add(authorityUserBan);

                Resource resourceUserUnban = new Resource().setRequestUrl("/user/{id}/unban").setRequestMethod("POST");
                resourceUserUnban.getResourceAuthorities().add(authorityUserUnban);



                Resource resourceResourcesGet = new Resource().setRequestUrl("/resource").setRequestMethod("GET");
                resourceResourcesGet.getResourceAuthorities().add(authorityResourceRead);

                Resource resourceResourceGet = new Resource().setRequestUrl("/resource/{id}").setRequestMethod("GET");
                resourceResourceGet.getResourceAuthorities().add(authorityResourceRead);

                Resource resourceResourceCreate = new Resource().setRequestUrl("/resource").setRequestMethod("POST");
                resourceResourceCreate.getResourceAuthorities().add(authorityResourceCreate);

                Resource resourceResourceUpdate = new Resource().setRequestUrl("/resource/{id}").setRequestMethod("PUT");
                resourceResourceUpdate.getResourceAuthorities().add(authorityResourceUpdate);

                Resource resourceResourcePatch = new Resource().setRequestUrl("/resource/{id}").setRequestMethod("PATCH");
                resourceResourcePatch.getResourceAuthorities().add(authorityResourcePatch);

                Resource resourceResourceDelete = new Resource().setRequestUrl("/resource/{id}").setRequestMethod("DELETE");
                resourceResourceDelete.getResourceAuthorities().add(authorityResourceDelete);



                List<Resource> resources = List.of(
                        resourceAuthoritiesGet,
                        resourceAuthorityGet,
                        resourceAuthorityCreate,
                        resourceAuthorityUpdate,
                        resourceAuthorityPatch,
                        resourceAuthorityDelete,

                        resourceRolesGet,
                        resourceRoleGet,
                        resourceRoleCreate,
                        resourceRoleUpdate,
                        resourceRolePatch,
                        resourceRoleDelete,

                        resourceUsersGet,
                        resourceUserGet,
                        resourceUserProfileImageGet,
                        resourceUserUpdate,
                        resourceUserPatch,
                        resourceUserDeactivate,
                        resourceUserUnlock,
                        resourceUserBan,
                        resourceUserUnban,

                        resourceResourcesGet,
                        resourceResourceGet,
                        resourceResourceCreate,
                        resourceResourceUpdate,
                        resourceResourcePatch,
                        resourceResourceDelete
                );

                resourceRepository.saveAll(resources);
            }
        };
    }

}