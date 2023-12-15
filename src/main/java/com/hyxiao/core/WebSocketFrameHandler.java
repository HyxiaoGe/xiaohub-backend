package com.hyxiao.core;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

/**
 * WebSocketFrameHandler 继承自 SimpleChannelInboundHandler，用于处理WebSocket帧。
 */
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    /**
     * 处理从客户端接收的每一个WebSocket帧
     * @param channelHandlerContext
     * @param webSocketFrame
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, WebSocketFrame webSocketFrame) throws Exception {
        // 判断接收的是否为文本帧
        if (webSocketFrame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textWebSocketFrame = (TextWebSocketFrame) webSocketFrame;
            System.out.println("WebSocket Received Message: " + textWebSocketFrame.text());
            channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame("我在呢~~~"));
        }

    }

}
