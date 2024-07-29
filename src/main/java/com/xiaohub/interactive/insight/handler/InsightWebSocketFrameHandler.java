package com.xiaohub.interactive.insight.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xiaohub.interactive.common.BasicMessage;
import com.xiaohub.util.JsonUtil;
import io.netty.channel.Channel;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsightWebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    public static final Logger log = LoggerFactory.getLogger(InsightWebSocketFrameHandler.class);

    //  用于存储所有活动的WebSocket连接
    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        channels.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        channels.remove(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // 这里可以处理从客户端接收到的WebSocket帧
        if (frame instanceof TextWebSocketFrame) {
            // 处理接收到的消息
        } else {
            // 处理其他类型的WebSocketFrame
            ctx.fireChannelRead(frame.retain());
        }
    }

    public static void broadcastUpdate(String msg) throws JsonProcessingException {
        TextWebSocketFrame response = new TextWebSocketFrame(JsonUtil.objectMapper.writeValueAsString(new BasicMessage(0, "update", msg)));
        for (Channel ctx : channels) {
            ctx.writeAndFlush(response.duplicate().retain());
        }
    }

}
