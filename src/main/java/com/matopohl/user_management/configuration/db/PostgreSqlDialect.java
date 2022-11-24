package com.matopohl.user_management.configuration.db;

import java.sql.Types;

@SuppressWarnings("unused")
public class PostgreSqlDialect extends org.hibernate.dialect.H2Dialect {

    public PostgreSqlDialect() {
        super();
        registerColumnType(Types.TINYINT, "smallint");
        registerColumnType(Types.VARBINARY, "bytea");
        registerColumnType(Types.BINARY, "uuid");
        registerColumnType(Types.TIMESTAMP, "timestamp with time zone");
    }

}
