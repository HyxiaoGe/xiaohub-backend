package com.hyxiao.config;

import java.io.InputStream;
import java.util.Properties;

public class LoadDiscordConfig {

    public static final String PROP_FILE = "discord.properties";
    private Properties properties;

    public LoadDiscordConfig() {
        properties = new Properties();

        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PROP_FILE);
            if (inputStream == null) {
                System.out.println("无法找到 " + PROP_FILE);
                return;
            }
            properties.load(inputStream);
        } catch (Exception ignored) {
        }
    }

    public String getUserToken() {
        return properties.getProperty("user_token");
    }

    public String getBotToken() {
        return properties.getProperty("bot_token");
    }

    public String getGuildId() {
        return properties.getProperty("guild_id");
    }

    public String getChannelId() {
        return properties.getProperty("channel_id");
    }

    public String getCallBackUrl() {
        return properties.getProperty("callback_url");
    }

//    public static void main(String[] args) {
//
//        LoadDiscordConfig loadDiscordConfig = new LoadDiscordConfig();
//        System.out.println(loadDiscordConfig.getUserToken());
//        System.out.println(loadDiscordConfig.getBotToken());
//        System.out.println(loadDiscordConfig.getGuildId());
//        System.out.println(loadDiscordConfig.getChannelId());
//        System.out.println(loadDiscordConfig.getCallBackUrl());
//
//    }

}
