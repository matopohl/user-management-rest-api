-- Database: user_management

begin;

    create user user_management_tech
        password 'user_management_tech';

    create schema if not exists public;

    create schema if not exists audit;

    revoke connect
        on database user_management
        from public;

    grant connect
    on database user_management
        to user_management_tech;

    grant usage
        on schema public
        to user_management_tech;

    grant usage
        on schema audit
        to user_management_tech;

commit;