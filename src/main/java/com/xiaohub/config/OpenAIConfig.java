package com.xiaohub.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public class OpenAIConfig {

    public static final String PROP_FILE = "chatgpt.properties";
    private Properties properties;

    public static final Logger log = LoggerFactory.getLogger(OpenAIConfig.class);

    public OpenAIConfig() {
        properties = new Properties();

        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PROP_FILE);
            if (inputStream == null) {
                log.error("无法找到: {}", PROP_FILE);
                return;
            }
            properties.load(inputStream);
        } catch (Exception ignored) {
        }
    }

    public String getChatModel() {
        return properties.getProperty("chat-model");
    }

    public String getImageModel() {
        return properties.getProperty("image-model");
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

    public String getAESKey() {
        return properties.getProperty("AESKEY");
    }

    public String getSecretKey() {
        return properties.getProperty("secretKey");
    }

    public String getAmount() {
        return properties.getProperty("amount");
    }

    public String getSize() {
        return properties.getProperty("size");
    }


}
