package com.nikitin.app.db.connection.manager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PropertiesUtil {
    private static final Properties PROPERTIES = new Properties();

    static {
        loadProperties();
    }

    private PropertiesUtil() {
    }

    private static void loadProperties() {
        try (InputStream inputstream = PropertiesUtil.class.getClassLoader()
                .getResourceAsStream("application.properties"))
        {
            PROPERTIES.load(inputstream);
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String get(String key) {
        return PROPERTIES.getProperty(key);

    }
}