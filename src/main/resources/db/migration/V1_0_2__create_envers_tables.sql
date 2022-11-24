-- Database: user_management

begin;

    create sequence if not exists audit.revinfo_seq;

    grant
        usage,
        select
        on sequence audit.revinfo_seq
        to user_management_tech;

    create table if not exists audit.revinfo (
        id integer default nextval('audit.revinfo_seq') not null unique primary key,
        timestamp bigint,

        request_id uuid unique,
        fk_log_id uuid unique
    );

    alter sequence audit.revinfo_seq
        owned by audit.revinfo.id;

    grant
        select,
        insert,
        update
        on table audit.revinfo
        to user_management_tech;

    create table if not exists audit.log (
        log_id uuid default uuid_generate_v4() not null unique primary key,
        date timestamp with time zone not null,
        user_id uuid,
        request_id uuid unique,
        request_url text not null,
        request_method text not null,
        request_content_type text,
        request_remote_address text not null,
        request_user_agent text,
        request_device text,
        request_session_id text,
        request text,
        response_status integer,
        response_content_type text,
        response text,

        fk_revinfo_id integer unique,

        constraint log_fk_revinfo_id foreign key (fk_revinfo_id) references audit.revinfo
    );

    grant
        select,
        insert
        on table audit.log
        to user_management_tech;

    alter table audit.revinfo
        add constraint revinfo_fk_log_id foreign key (fk_log_id) references audit.log;

commit;