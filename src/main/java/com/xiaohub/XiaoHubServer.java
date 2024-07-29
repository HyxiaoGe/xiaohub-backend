package com.xiaohub;

import com.xiaohub.datadigger.DataAgent;
import com.xiaohub.interactive.chat.initializer.ChatServerInitializer;
import com.xiaohub.interactive.image.initializer.ImageServerInitializer;
import com.xiaohub.interactive.insight.initializer.InsightServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XiaoHubServer {

    private static final Logger log = LoggerFactory.getLogger(XiaoHubServer.class);

    private static final EventLoopGroup masterGroup = new NioEventLoopGroup(5);
    private static final EventLoopGroup workerGroup = new NioEventLoopGroup();

    public static void main(String[] args) {

        startServer(new ChatServerInitializer(), 8808);
        startServer(new ImageServerInitializer(), 8809);
        startServer(new InsightServerInitializer(), 8810);

        DataAgent.init();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            masterGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }));
    }

    private static void startServer(ChannelInitializer<SocketChannel> serverInitializer, int port) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(masterGroup, workerGroup)
               .channel(NioServerSocketChannel.class)
               .childHandler(serverInitializer);

        try {
            Channel channel = bootstrap.bind(port).channel();
            log.info("Server started on port: {}", port);
            channel.closeFuture().addListener(future -> {
                if (future.isSuccess()) {
                    log.info("Server on port: {} closed successfully", port);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
