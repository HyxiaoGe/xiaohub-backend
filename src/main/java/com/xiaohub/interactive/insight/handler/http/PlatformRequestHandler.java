package com.xiaohub.interactive.insight.handler.http;

import com.xiaohub.datadigger.DataAgent;
import com.xiaohub.datadigger.dto.Article;
import com.xiaohub.util.HttpResponseUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class PlatformRequestHandler implements RequestHandler{

    public static final Logger log = LoggerFactory.getLogger(PlatformRequestHandler.class);

    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        Map<String, List<String>> parameters = decoder.parameters();
        String platform = parameters.get("platform").get(0);
        log.info("platform: {}", platform);
        List<Article> articles = DataAgent.retrieve(platform);
        HttpResponseUtil.sendRequest(ctx, articles);
    }
}
