package com.xiaohub.interactive.image.initializer;

import com.xiaohub.interactive.image.handler.ImageWebSocketFrameHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class ImageServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        //  HttpServerCodec: 编解码器，用于将字节解码为HTTP请求和编码HTTP响应
        socketChannel.pipeline().addLast(new HttpServerCodec());
        //  HttpObjectAggregator: 将HTTP消息的多个部分组合成一个完整的HTTP消息 （限制为 256KB * 1024 = 262144）
        socketChannel.pipeline().addLast(new HttpObjectAggregator(262144));
        //  ChunkedWriteHandler: 用于异步写大的数据流（例如文件的内容）
        socketChannel.pipeline().addLast(new ChunkedWriteHandler());
        //  WebSocketServerProtocolHandler: 处理特定于WebSocket的事务，例如握手和帧的控制
        socketChannel.pipeline().addLast(new WebSocketServerProtocolHandler("/ws",  null, true, 262144));
        //  WebSocketFrameHandler 自定义的处理器，用于处理WebSocket
        socketChannel.pipeline().addLast(new ImageWebSocketFrameHandler());
    }
}
