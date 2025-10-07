package com.nikitin.app.db.connection.manager;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class PostgresConnectionManager {
    private static final String USERNAME_KEY = "db.username";
    private static final String PASSWORD_KEY = "db.password";
    private static final String URL_KEY = "db.url";
    private static final String POOL_SIZE_KEY = "db.pool_size";
    private static final String DB_DRIVER_KEY = "db.driver";
    private static final Integer DEFAULT_POOL_SIZE = 10;
    private static BlockingQueue<Connection> pool;
    private static List<Connection> sourceConnections;

    static {
        loadDriver();
        initConnectionPool();
    }

    private static void initConnectionPool() {
        String poolSize = PropertiesUtil.get(POOL_SIZE_KEY);
        Integer size = poolSize == null ? DEFAULT_POOL_SIZE : Integer.parseInt(poolSize);
        pool = new ArrayBlockingQueue<>(size);
        sourceConnections = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Connection connection = open();
            Connection proxyConnection = (Connection) Proxy.newProxyInstance(PostgresConnectionManager.class.getClassLoader(), new Class[]{Connection.class},
                    ((proxy, method, args) -> method.getName().equals("close") ? pool.add((Connection) proxy) : method.invoke(connection, args)));
            pool.add(proxyConnection);
            sourceConnections.add(connection);
        }
    }

    private static void loadDriver() {
        try {
            String driverClassName = PropertiesUtil.get(DB_DRIVER_KEY);
            if (driverClassName == null) {
                throw new RuntimeException("Driver not found in application.properties: " + DB_DRIVER_KEY);
            }
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load database driver", e);
        }
    }

    private PostgresConnectionManager() {
    }

    public static Connection get() {
        try {
            return pool.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static Connection open() {
        try {
            return DriverManager.getConnection(
                    PropertiesUtil.get(URL_KEY),
                    PropertiesUtil.get(USERNAME_KEY),
                    PropertiesUtil.get(PASSWORD_KEY)
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void closePool() {
        for (Connection sourceConnection : sourceConnections) {
            try {
                sourceConnection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}