package net.lunark.io.database;

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
