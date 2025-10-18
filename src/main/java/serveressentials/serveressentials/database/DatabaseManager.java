package serveressentials.serveressentials.database;


import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class DatabaseManager {

    private Connection connection;
    private final String type;
    private final String host, database, username, password;
    private final int port;
    private final File dataFolder;

    public DatabaseManager(String type, String host, int port, String database,
                           String username, String password, File dataFolder) {
        this.type = type.toLowerCase();
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.dataFolder = dataFolder;
    }

    public void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) return;

        if (type.equals("mysql")) {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true",
                    username, password);
        } else if (type.equals("sqlite")) {
            File dbFile = new File(dataFolder, database + ".db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        } else {
            throw new SQLException("Unknown database type: " + type);
        }
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) connect();
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void executeUpdate(String sql, Object... params) {
        try (PreparedStatement ps = prepare(sql, params)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void executeUpdateAsync(String sql, Object... params) {
        Bukkit.getScheduler().runTaskAsynchronously(
                Bukkit.getPluginManager().getPlugin("YourPluginName"),
                () -> executeUpdate(sql, params));
    }

    public void executeQuery(String sql, Consumer<ResultSet> consumer, Object... params) {
        try (PreparedStatement ps = prepare(sql, params);
             ResultSet rs = ps.executeQuery()) {
            consumer.accept(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void executeQueryAsync(String sql, Consumer<ResultSet> consumer, Object... params) {
        Bukkit.getScheduler().runTaskAsynchronously(
                Bukkit.getPluginManager().getPlugin("YourPluginName"),
                () -> executeQuery(sql, consumer, params));
    }

    public CompletableFuture<ResultSet> queryFuture(String sql, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PreparedStatement ps = prepare(sql, params);
                return ps.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    private PreparedStatement prepare(String sql, Object... params) throws SQLException {
        PreparedStatement ps = getConnection().prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
        return ps;
    }
}

