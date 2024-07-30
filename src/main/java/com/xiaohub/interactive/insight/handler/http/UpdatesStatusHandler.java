package com.xiaohub.interactive.insight.handler.http;

import com.xiaohub.interactive.insight.model.PlatformUpdateStatus;
import com.xiaohub.util.HttpResponseUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;

public class UpdatesStatusHandler implements RequestHandler{
    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {

        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        Map<String, List<String>> parameters = decoder.parameters();
        boolean reset = parameters.containsKey("reset") && "true".equals(parameters.get("reset").get(0));
        if (reset) {
            PlatformUpdateStatus.resetAllStatuses();
        }

        Map<String, Boolean> updateStatuses = PlatformUpdateStatus.getAllStatus();
        HttpResponseUtil.sendRequest(ctx, updateStatuses);
    }
}
