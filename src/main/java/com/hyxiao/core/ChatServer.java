package com.hyxiao.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatServer {

    public static final Logger log = LoggerFactory.getLogger(ChatServer.class);

    public void start() {

        log.info("Chat Server started successfully!!!");

        //  创建两个 EventLoopGroup 对象
        //  masterGroup 通常用来接收客户端的TCP连接
        NioEventLoopGroup masterGroup = new NioEventLoopGroup();
        //  workerGroup 用来处理已经被接收的连接，如处理I/O操作
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            //  serverBootstrap 是一个启动NIO服务的辅助启动类，用于设置服务器
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(masterGroup, workerGroup).channel(NioServerSocketChannel.class)  // 设置服务器使用的channel类型，适用于NIO传输。
                    .childHandler(new ServerInitializer()); // 设置自定义处理类，当一个新的连接被接收时会使用的 ChannelInitializer

            //  绑定端口，开始接收进来的连接
            serverBootstrap.bind(8088) //  绑定服务器到指定的端口并开始监听
                    .sync()     //  方法会堵塞，直到绑定操作完成。
                    .channel().closeFuture().sync();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            //  关闭线程组，释放资源
            workerGroup.shutdownGracefully();
            masterGroup.shutdownGracefully();
        }
    }


}
