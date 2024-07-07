package com.xiaohub.interactive.chat;

import com.xiaohub.interactive.chat.initializer.ChatServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatServer {

    public static final Logger log = LoggerFactory.getLogger(ChatServer.class);

    public void start() {
        log.info("Chat Server starting!!! ");
        NioEventLoopGroup masterGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(masterGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChatServerInitializer());
            serverBootstrap.bind(8808)
                    .sync()
                    .channel().closeFuture().sync();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            workerGroup.shutdownGracefully();
            masterGroup.shutdownGracefully();
        }
    }

}
