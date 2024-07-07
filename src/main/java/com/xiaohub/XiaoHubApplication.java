package com.xiaohub;

import com.xiaohub.interactive.chat.ChatServer;

public class XiaoHubApplication {
    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        try {
            chatServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
