-- Database: user_management

begin;

    create table if not exists audit.documents_audit (
        document_id uuid not null,
        content bytea not null,

        fk_revinfo_id integer not null,
        revinfo_type smallint not null,

        primary key (document_id, fk_revinfo_id),
        constraint roles_roles_audit_fk_revinfo_id foreign key (fk_revinfo_id) references audit.revinfo
    );

    grant
        select,
        insert
        on table audit.documents_audit
        to user_management_tech;

    create table if not exists audit.users_audit (
        user_id uuid not null,
        first_name text not null,
        last_name text not null,
        username text,
        email text not null,
        password text not null,
        verified boolean not null,
        active boolean not null,
        lock boolean not null,
        ban boolean not null,
        creation_date timestamp with time zone not null,
        fk_profile_image uuid not null,

        fk_revinfo_id integer not null,
        revinfo_type smallint not null,

        primary key (user_id, fk_revinfo_id),
        constraint users_audit_fk_revinfo_id foreign key (fk_revinfo_id) references audit.revinfo
    );

    grant
        select,
        insert
        on table audit.users_audit
        to user_management_tech;

    create table if not exists audit.roles_audit (
        role_id uuid not null,
        name text,

        fk_revinfo_id integer not null,
        revinfo_type smallint not null,

        primary key (role_id, fk_revinfo_id),
        constraint roles_audit_fk_revinfo_id foreign key (fk_revinfo_id) references audit.revinfo
    );

    grant
        select,
        insert
        on table audit.roles_audit
        to user_management_tech;

    create table if not exists audit.roles_roles_audit (
        fk_role_id uuid not null,
        fk_parent_role_id uuid not null,

        fk_revinfo_id integer not null,
        revinfo_type smallint not null,

        primary key (fk_role_id, fk_parent_role_id, fk_revinfo_id),
        constraint roles_roles_audit_fk_revinfo_id foreign key (fk_revinfo_id) references audit.revinfo
    );

    grant
        select,
        insert
        on table audit.roles_roles_audit
        to user_management_tech;

    create table if not exists audit.users_roles_audit (
        fk_user_id uuid not null,
        fk_role_id uuid not null,

        fk_revinfo_id integer not null,
        revinfo_type smallint not null,

        primary key (fk_user_id, fk_role_id, fk_revinfo_id),
        constraint users_roles_audit_fk_revinfo_id foreign key (fk_revinfo_id) references audit.revinfo
    );

    grant
        select,
        insert
        on table audit.users_roles_audit
        to user_management_tech;

    create table if not exists audit.authorities_audit (
        authority_id uuid not null,
        name text not null,

        fk_revinfo_id integer not null,
        revinfo_type smallint not null,

        primary key (authority_id, fk_revinfo_id),
        constraint authorities_audit_fk_revinfo_id foreign key (fk_revinfo_id) references audit.revinfo
    );

    grant
        select,
        insert
        on table audit.authorities_audit
        to user_management_tech;

    create table if not exists audit.roles_authorities_audit (
        fk_role_id uuid not null,
        fk_authority_id uuid not null,

        fk_revinfo_id integer not null,
        revinfo_type smallint not null,

        primary key (fk_role_id, fk_authority_id, fk_revinfo_id),
        constraint roles_authorities_audit_fk_revinfo_id foreign key (fk_revinfo_id) references audit.revinfo
    );

    grant
        select,
        insert
        on table audit.roles_authorities_audit
        to user_management_tech;

    create table if not exists audit.users_authorities_audit (
        fk_user_id uuid not null,
        fk_authority_id uuid not null,

        fk_revinfo_id integer not null,
        revinfo_type smallint not null,

        primary key (fk_user_id, fk_authority_id, fk_revinfo_id),
        constraint users_authorities_audit_fk_revinfo_id foreign key (fk_revinfo_id) references audit.revinfo
    );

    grant
        select,
        insert
        on table audit.users_authorities_audit
        to user_management_tech;

    create table if not exists audit.user_devices_audit (
        user_device_id uuid not null,
        name text not null,
        user_agent text not null,
        fk_user_id uuid not null,

        fk_revinfo_id integer not null,
        revinfo_type smallint not null,

        primary key (user_device_id, fk_revinfo_id),
        constraint users_audit_fk_revinfo_id foreign key (fk_revinfo_id) references audit.revinfo
    );

    grant
        select,
        insert
        on table audit.user_devices_audit
        to user_management_tech;

    create table if not exists audit.resources_audit (
        resource_id uuid not null,
        request_url text not null,
        request_method text not null,

        fk_revinfo_id integer not null,
        revinfo_type smallint not null,

        primary key (resource_id, fk_revinfo_id),
        constraint resources_audit_fk_revinfo_id foreign key (fk_revinfo_id) references audit.revinfo
    );

    grant
        select,
        insert
        on table audit.resources_audit
        to user_management_tech;

    create table if not exists audit.resources_authorities_audit (
        fk_resource_id uuid not null,
        fk_authority_id uuid not null,

        fk_revinfo_id integer not null,
        revinfo_type smallint not null,

        primary key (fk_resource_id, fk_authority_id, fk_revinfo_id),
        constraint resources_authorities_audit_fk_revinfo_id foreign key (fk_revinfo_id) references audit.revinfo
        );

    grant
        select,
        insert
        on table audit.resources_authorities_audit
        to user_management_tech;

    create table if not exists audit.refresh_tokens_audit (
        refresh_token_id uuid not null,
        creation_date timestamp with time zone not null,
        expiration_date timestamp with time zone,
        last_refresh_date timestamp with time zone,
        refresh_count bigint not null,
        fk_user_device_id uuid not null,

        fk_revinfo_id integer not null,
        revinfo_type smallint not null,

        primary key (refresh_token_id, fk_revinfo_id),
        constraint users_audit_fk_revinfo_id foreign key (fk_revinfo_id) references audit.revinfo
    );

    grant
        select,
        insert
        on table audit.refresh_tokens_audit
        to user_management_tech;

    create table if not exists audit.verify_user_tokens_audit (
        verify_user_token_id uuid not null,
        creation_date timestamp with time zone not null,
        expiration_date timestamp with time zone,
        verify_date timestamp with time zone,
        fk_user_id uuid not null,

        fk_revinfo_id integer not null,
        revinfo_type smallint not null,

        primary key (verify_user_token_id, fk_revinfo_id),
        constraint verify_tokens_audit_fk_revinfo_id foreign key (fk_revinfo_id) references audit.revinfo
    );

    grant
        select,
        insert
        on table audit.verify_user_tokens_audit
        to user_management_tech;

    create table if not exists audit.reset_user_password_tokens_audit (
        reset_user_password_token_id uuid not null,
        creation_date timestamp with time zone not null,
        expiration_date timestamp with time zone,
        verify_date timestamp with time zone,
        fk_user_id uuid not null,

        fk_revinfo_id integer not null,
        revinfo_type smallint not null,

        primary key (reset_user_password_token_id, fk_revinfo_id),
        constraint reset_user_password_tokens_audit_fk_revinfo_id foreign key (fk_revinfo_id) references audit.revinfo
    );

    grant
        select,
        insert
        on table audit.reset_user_password_tokens_audit
        to user_management_tech;

commit;