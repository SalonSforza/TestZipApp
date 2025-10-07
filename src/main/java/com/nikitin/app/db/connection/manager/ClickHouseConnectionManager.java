package com.nikitin.app.db.connection.manager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ClickHouseConnectionManager {

    private static final String USERNAME_KEY = "clickhouse.user";
    private static final String PASSWORD_KEY = "clickhouse.password";
    private static final String URL_KEY = "clickhouse.url";
    private static final String DRIVER_KEY = "clickhouse.driver";

    static {
        loadDriver();
    }

    private static void loadDriver() {
        try {
            String driver = PropertiesUtil.get(DRIVER_KEY);
            if (driver == null || driver.isEmpty()) {
                driver = "com.clickhouse.jdbc.ClickHouseDriver";
            }
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("ClickHouse JDBC driver not found", e);
        }
    }

    public static Connection get() {
        try {
            return DriverManager.getConnection(
                    PropertiesUtil.get(URL_KEY),
                    PropertiesUtil.get(USERNAME_KEY),
                    PropertiesUtil.get(PASSWORD_KEY)
            );
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка подключения к ClickHouse", e);
        }
    }
}
