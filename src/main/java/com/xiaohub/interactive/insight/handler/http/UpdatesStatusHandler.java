package com.xiaohub.interactive.insight.handler.http;

import com.xiaohub.datadigger.DataAgent;
import com.xiaohub.interactive.insight.model.PlatformStatus;
import com.xiaohub.util.HttpResponseUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.Map;

public class UpdatesStatusHandler implements RequestHandler{
    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        Map<String, PlatformStatus> updateStatuses = DataAgent.getAllStatus();
        HttpResponseUtil.sendRequest(ctx, updateStatuses);
    }
}
