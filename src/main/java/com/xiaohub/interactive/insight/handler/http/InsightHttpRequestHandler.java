package com.xiaohub.interactive.insight.handler.http;

import com.xiaohub.util.HttpResponseUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class InsightHttpRequestHandler extends SimpleChannelInboundHandler<HttpObject> {

    public static final Logger log = LoggerFactory.getLogger(InsightHttpRequestHandler.class);

    private final Map<String, RequestHandler> routeTable;

    public InsightHttpRequestHandler() {
        routeTable = new HashMap<>();
        routeTable.put("/api/insight", new PlatformRequestHandler());
        routeTable.put("/api/insight/updates-status", new UpdatesStatusHandler());
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            String path = new QueryStringDecoder(request.uri()).path();
            RequestHandler handler = routeTable.get(path);

            if (handler != null) {
                handler.handle(ctx, request);
            } else {
                HttpResponseUtil.sendNotFound(ctx);
            }
        }
    }

}
