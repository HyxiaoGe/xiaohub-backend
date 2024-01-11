package com.hyxiao;

import com.hyxiao.core.ChatServer;

public class YunChatApplication {
    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
