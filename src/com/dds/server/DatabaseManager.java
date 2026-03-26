package com.dds.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private final String url;
    private final String username;
    private final String password;

    public DatabaseManager() {
        this.url = readConfig("dds.db.url", "DDS_DB_URL",
                "jdbc:mysql://127.0.0.1:3306/distributed_drinks_business?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        this.username = readConfig("dds.db.user", "DDS_DB_USER", "root");
        this.password = readConfig("dds.db.password", "DDS_DB_PASSWORD", "");
        loadDriver();
    }

    public Connection openConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    private void loadDriver() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException(
                    "MySQL JDBC driver not found. Add mysql-connector-j to the runtime classpath.",
                    exception
            );
        }
    }

    private String readConfig(String propertyName, String envName, String defaultValue) {
        String propertyValue = System.getProperty(propertyName);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue.trim();
        }

        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        return defaultValue;
    }
}
