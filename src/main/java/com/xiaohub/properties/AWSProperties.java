package com.xiaohub.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public class AWSProperties {

    public static final String PROP_FILE = "aws.properties";
    private Properties properties;

    public static final Logger log = LoggerFactory.getLogger(AWSProperties.class);

    public AWSProperties() {
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

    public String getProxyUrl() {
        return properties.getProperty("proxyUrl");
    }

    public String getDatadiggerUrl() {
        return properties.getProperty("datadiggerUrl");
    }
}
