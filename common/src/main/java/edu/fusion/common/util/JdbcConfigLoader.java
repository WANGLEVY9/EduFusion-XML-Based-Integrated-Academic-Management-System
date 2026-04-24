package edu.fusion.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class JdbcConfigLoader {

    private JdbcConfigLoader() {
    }

    public static JdbcConfig loadFromClasspath(String resourcePath) {
        InputStream inputStream = JdbcConfigLoader.class.getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IllegalStateException("JDBC config resource not found: " + resourcePath);
        }
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load JDBC config resource: " + resourcePath, e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException ignored) {
            }
        }
        JdbcConfig config = new JdbcConfig();
        config.setDriverClass(properties.getProperty("driverClass", ""));
        config.setUrl(properties.getProperty("url", ""));
        config.setUsername(properties.getProperty("username", ""));
        config.setPassword(properties.getProperty("password", ""));
        return config;
    }
}
