package com.hyxiao.config;

import java.io.InputStream;
import java.util.Properties;

public class LoaderConfig {

    public static final String PROP_FILE = "chatgpt.properties";
    private Properties properties;

    public LoaderConfig () {
        properties = new Properties();

        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PROP_FILE);
            if (inputStream == null) {
                System.out.println("无法找到 " + PROP_FILE);
                return;
            }
            properties.load(inputStream);
        }catch (Exception ignored){}
    }

    public String getModel() {
        return properties.getProperty("model");
    }

    public double getTemperature() {
        return Double.parseDouble(properties.getProperty("temperature"));
    }

    public int getMaxTokens() {
        return Integer.parseInt(properties.getProperty("maxTokens"));
    }

    public String getProxyUrl() {
        return properties.getProperty("proxyUrl");
    }

    public String getApiKeys() {
        return properties.getProperty("apiKeys");
    }

}
