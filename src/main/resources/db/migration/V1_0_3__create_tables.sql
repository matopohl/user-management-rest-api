-- Database: user_management

begin;

    create table if not exists documents (
        document_id uuid default uuid_generate_v4() not null unique primary key,
        content bytea not null
    );

    grant
        select,
        insert,
        update,
        delete
        on table documents
        to user_management_tech;

    create table if not exists users (
        user_id uuid default uuid_generate_v4() not null unique primary key,
        first_name text not null,
        last_name text not null,
        username text unique,
        email text not null unique,
        password text not null,
        verified boolean not null,
        active boolean not null,
        lock boolean not null,
        ban boolean not null,
        creation_date timestamp with time zone not null,
        fk_profile_image uuid not null,

        constraint users_fk_profile_image_id foreign key (fk_profile_image) references documents
    );

    grant
        select,
        insert,
        update,
        delete
        on table users
        to user_management_tech;

    create table if not exists roles (
        role_id uuid default uuid_generate_v4() not null unique primary key,
        name text not null unique
    );

    grant
        select,
        insert,
        update,
        delete
        on table roles
        to user_management_tech;

    create table if not exists roles_roles (
        fk_role_id uuid not null,
        fk_parent_role_id uuid not null,

        constraint roles_roles_fk_role_id foreign key (fk_role_id) references roles,
        constraint roles_roles_fk_parent_role_id foreign key (fk_parent_role_id) references roles
    );

    grant
        select,
        insert,
        update,
        delete
        on table roles_roles
        to user_management_tech;

    create table if not exists users_roles (
        fk_user_id uuid not null,
        fk_role_id uuid not null,

        constraint users_roles_fk_user_id foreign key (fk_user_id) references users,
        constraint users_roles_fk_role_id foreign key (fk_role_id) references roles
    );

    grant
        select,
        insert,
        update,
        delete
        on table users_roles
        to user_management_tech;

    create table if not exists authorities (
        authority_id uuid default uuid_generate_v4() not null unique primary key,
        name text not null unique
    );

    grant
        select,
        insert,
        update,
        delete
        on table authorities
        to user_management_tech;

    create table if not exists roles_authorities (
        fk_role_id uuid not null,
        fk_authority_id uuid not null,

        constraint roles_authorities_fk_role_id foreign key (fk_role_id) references roles,
        constraint roles_authorities_fk_authority_id foreign key (fk_authority_id) references authorities
    );

    grant
        select,
        insert,
        update,
        delete
        on table roles_authorities
        to user_management_tech;

    create table if not exists users_authorities (
        fk_user_id uuid not null,
        fk_authority_id uuid not null,

        constraint users_authorities_fk_user_id foreign key (fk_user_id) references users,
        constraint users_authorities_fk_authority_id foreign key (fk_authority_id) references authorities
    );

    grant
        select,
        insert,
        update,
        delete
        on table users_authorities
        to user_management_tech;

    create table if not exists user_devices (
        user_device_id uuid default uuid_generate_v4() not null unique primary key,
        name text not null,
        user_agent text not null,
        fk_user_id uuid not null,

        constraint user_devicess_fk_user_id foreign key (fk_user_id) references users
    );

    grant
        select,
        insert,
        update,
        delete
        on table user_devices
        to user_management_tech;

    create table if not exists resources (
        resource_id uuid default uuid_generate_v4() not null unique primary key,
        request_url text not null,
        request_method text not null,

        constraint resources_unique_request_url_request_method unique(request_url, request_method)
    );

    grant
        select,
        insert,
        update,
        delete
        on table resources
        to user_management_tech;

    create table if not exists resources_authorities (
        fk_resource_id uuid not null,
        fk_authority_id uuid not null,

        constraint resources_authorities_fk_resource_id foreign key (fk_resource_id) references resources,
        constraint resources_authorities_fk_authority_id foreign key (fk_authority_id) references authorities
    );

    grant
        select,
        insert,
        update,
        delete
        on table resources_authorities
        to user_management_tech;

    create table if not exists refresh_tokens (
        refresh_token_id uuid default uuid_generate_v4() not null unique primary key,
        creation_date timestamp with time zone not null,
        expiration_date timestamp with time zone,
        last_refresh_date timestamp with time zone,
        refresh_count bigint not null,
        fk_user_device_id uuid not null unique,

        constraint refresh_tokens_fk_user_device_id foreign key (fk_user_device_id) references user_devices
    );

    grant
        select,
        insert,
        update,
        delete
        on table refresh_tokens
        to user_management_tech;

    create table if not exists verify_user_tokens (
        verify_user_token_id uuid default uuid_generate_v4() not null unique primary key,
        creation_date timestamp with time zone not null,
        expiration_date timestamp with time zone,
        verify_date timestamp with time zone,
        fk_user_id uuid not null unique,

        constraint verify_user_tokens_fk_user_id foreign key (fk_user_id) references users
    );

    grant
        select,
        insert,
        update,
        delete
        on table verify_user_tokens
        to user_management_tech;

    create table if not exists reset_user_password_tokens (
        reset_user_password_token_id uuid default uuid_generate_v4() not null unique primary key,
        creation_date timestamp with time zone not null,
        expiration_date timestamp with time zone,
        verify_date timestamp with time zone,
        fk_user_id uuid not null unique,

        constraint reset_user_password_tokens_fk_user_id foreign key (fk_user_id) references users
    );

    grant
        select,
        insert,
        update,
        delete
        on table reset_user_password_tokens
        to user_management_tech;

commit;