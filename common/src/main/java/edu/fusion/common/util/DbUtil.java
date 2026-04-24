package edu.fusion.common.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DbUtil {

    private DbUtil() {
    }

    public static Connection getSqlServerConnection(String url, String user, String password) throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public static Connection getOracleConnection(String url, String user, String password) throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public static Connection getMySqlConnection(String url, String user, String password) throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public static Connection getConnection(JdbcConfig config) throws SQLException {
        if (config.getDriverClass() != null && !config.getDriverClass().trim().isEmpty()) {
            try {
                Class.forName(config.getDriverClass());
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("JDBC driver not found: " + config.getDriverClass(), e);
            }
        }
        return DriverManager.getConnection(config.getUrl(), config.getUsername(), config.getPassword());
    }
}
