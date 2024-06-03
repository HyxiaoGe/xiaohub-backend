package com.xiaohub;

import com.xiaohub.interactive.chat.ChatServer;

public class XiaoHubApplication {
    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
