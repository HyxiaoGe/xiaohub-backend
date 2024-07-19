package com.xiaohub.interactive.chat.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ExceptionHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(ExceptionHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException && "Connection reset by peer".equals(cause.getMessage())) {
            log.warn("Connection reset by peer");
        } else {
            log.error("Unexpected exception caught", cause);
        }
        ctx.close();
    }
}
