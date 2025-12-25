package net.godlycow.org.database;

import net.godlycow.org.database.type.DatabaseType;

public record DatabaseConfig(
        DatabaseType type,
        String sqliteFile,
        String mysqlHost,
        int mysqlPort,
        String mysqlDatabase,
        String mysqlUser,
        String mysqlPassword,
        int poolSize
) {}
