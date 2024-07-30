package com.xiaohub.interactive.insight.handler.http;

import com.xiaohub.datadigger.DataAgent;
import com.xiaohub.datadigger.dto.Article;
import com.xiaohub.util.HttpResponseUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;

public class PlatformRequestHandler implements RequestHandler{
    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        Map<String, List<String>> parameters = decoder.parameters();
        String platform = parameters.get("platform").get(0);
        List<Article> articles = DataAgent.retrieve(platform);
        HttpResponseUtil.sendRequest(ctx, articles);
    }
}
