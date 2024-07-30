package com.xiaohub.interactive.insight.handler.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public interface RequestHandler {
    void handle(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception;
}
