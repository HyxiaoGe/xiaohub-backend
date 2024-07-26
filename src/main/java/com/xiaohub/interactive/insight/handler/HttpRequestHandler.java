package com.xiaohub.interactive.insight.handler;

import com.xiaohub.datadigger.DataAgent;
import com.xiaohub.datadigger.dto.Article;
import com.xiaohub.util.JsonUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class HttpRequestHandler extends SimpleChannelInboundHandler<HttpObject> {

    public static final Logger log = LoggerFactory.getLogger(HttpRequestHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            String uri = request.uri();
            if (uri.startsWith("/api/insight")) {
                String platform = uri.substring(uri.lastIndexOf("/") + 1);
                List<Article> articles = DataAgent.retrieve(platform);

                String jsonString = JsonUtil.objectMapper.writeValueAsString(articles);
                DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK,
                        Unpooled.copiedBuffer(jsonString, StandardCharsets.UTF_8)
                );
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

}
