package com.xiaohub.interactive.image;

import com.xiaohub.interactive.image.initializer.ImageServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageServer {

    public static final Logger log = LoggerFactory.getLogger(ImageServer.class);

    public void start() {
        log.info("Image Server starting!!! ");
        NioEventLoopGroup masterGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(masterGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ImageServerInitializer());
            serverBootstrap.bind(8809)
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
