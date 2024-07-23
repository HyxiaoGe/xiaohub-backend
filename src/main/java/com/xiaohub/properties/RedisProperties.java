package com.xiaohub.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class RedisProperties {
    private static Properties properties;

    static {
        properties = new Properties();
        try (InputStream input = RedisProperties.class.getClassLoader().getResourceAsStream("redis.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find redis.properties");
            } else {
                properties.load(input);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static int getIntProperty(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }

    public static boolean getBooleanProperty(String key) {
        return Boolean.parseBoolean(properties.getProperty(key));
    }

}

