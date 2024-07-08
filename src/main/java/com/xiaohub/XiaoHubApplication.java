package com.xiaohub;

import com.xiaohub.interactive.chat.ChatServer;
import com.xiaohub.interactive.image.ImageServer;

public class XiaoHubApplication {
    public static void main(String[] args) {
//        ChatServer chatServer = new ChatServer();
        ImageServer imageServer = new ImageServer();
        try {
//            chatServer.start();
            imageServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
