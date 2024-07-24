package com.xiaohub.interactive.insight.initializer;

import com.xiaohub.interactive.insight.handler.HttpRequestHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * ServerInitializer 继承自 ChannelInitializer，用于初始化新接受的通道。
 */
public class InsightServerInitializer extends ChannelInitializer<SocketChannel> {

    /**
     * 为新的 SocketChannel 设置了 ChannelPipeline 和各种 ChannelHandler。
     *
     * @param socketChannel
     */
    @Override
    protected void initChannel(SocketChannel socketChannel) {
        //  HttpServerCodec: 编解码器，用于将字节解码为HTTP请求和编码HTTP响应
        socketChannel.pipeline().addLast(new HttpServerCodec());
        //  HttpObjectAggregator: 将HTTP消息的多个部分组合成一个完整的HTTP消息 （限制为 256KB * 1024 = 262144）
        socketChannel.pipeline().addLast(new HttpObjectAggregator(10485760));
        //  ChunkedWriteHandler: 用于异步写大的数据流（例如文件的内容）
        socketChannel.pipeline().addLast(new ChunkedWriteHandler());
        //  HttpRequestHandler 自定义的处理器，用于处理HTTP请求
        socketChannel.pipeline().addLast(new HttpRequestHandler());
    }
}
