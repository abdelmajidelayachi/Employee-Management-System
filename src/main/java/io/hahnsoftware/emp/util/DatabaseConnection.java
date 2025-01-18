package io.hahnsoftware.emp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;

/**
 * this class for connection to database, we use singleton to optimize connections objects
 */
public class DatabaseConnection {
    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Properties props = new Properties();
                // Load properties from classpath
                try (InputStream inputStream = DatabaseConnection.class.getClassLoader()
                        .getResourceAsStream("database.properties")) {
                    if (inputStream == null) {
                        throw new IOException("Properties file 'database.properties' not found in classpath");
                    }
                    props.load(inputStream);
                }

                String url = props.getProperty("db.url");
                String user = props.getProperty("db.user");
                String password = props.getProperty("db.password");

                if (url == null || user == null || password == null) {
                    throw new IOException("Database properties are missing required values");
                }

                // Register JDBC driver
                Class.forName("oracle.jdbc.OracleDriver");

                connection = DriverManager.getConnection(url, user, password);
                connection.setAutoCommit(false); // For transaction management

            } catch (IOException e) {
                throw new SQLException("Could not load database properties: " + e.getMessage(), e);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Oracle JDBC Driver not found", e);
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void commitTransaction() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.commit();
        }
    }

    public static void rollbackTransaction() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.rollback();
                }
            } catch (SQLException e) {
                System.err.println("Error rolling back transaction: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}