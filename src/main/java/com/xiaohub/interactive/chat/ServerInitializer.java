package com.xiaohub.interactive.chat;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ServerInitializer 继承自 ChannelInitializer，用于初始化新接受的通道。
 */
public class ServerInitializer extends ChannelInitializer<SocketChannel> {
    public static final Logger log = LoggerFactory.getLogger(ServerInitializer.class);

    /**
     * 为新的 SocketChannel 设置了 ChannelPipeline 和各种 ChannelHandler。
     *
     * @param socketChannel
     * @throws Exception
     */
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        //  HttpServerCodec: 编解码器，用于将字节解码为HTTP请求和编码HTTP响应
        socketChannel.pipeline().addLast(new HttpServerCodec());
        //  HttpObjectAggregator: 将HTTP消息的多个部分组合成一个完整的HTTP消息
        socketChannel.pipeline().addLast(new HttpObjectAggregator(65536));
        //  ChunkedWriteHandler: 用于异步写大的数据流（例如文件的内容）
        socketChannel.pipeline().addLast(new ChunkedWriteHandler());
        //  WebSocketServerProtocolHandler: 处理特定于WebSocket的事务，例如握手和帧的控制
        socketChannel.pipeline().addLast(new WebSocketServerProtocolHandler("/ws"));
        //  WebSocketFrameHandler 自定义的处理器，用于处理WebSocket
        socketChannel.pipeline().addLast(new WebSocketFrameHandler());
    }
}
