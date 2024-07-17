package com.xiaohub.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public class BaiDuConfig {

    public static final String PROP_FILE = "baidu.properties";
    private Properties properties;

    public static final Logger log = LoggerFactory.getLogger(BaiDuConfig.class);

    public BaiDuConfig() {
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

    public String getAppId() {
        return properties.getProperty("appid");
    }

    public String getAppSecret() {
        return properties.getProperty("appsecret");
    }

}
