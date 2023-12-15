package com.hyxiao.core;

import com.hyxiao.config.LoaderConfig;
import com.hyxiao.model.Message;
import com.hyxiao.model.Payload;
import com.hyxiao.util.HttpUtil;
import com.hyxiao.util.JsonUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.apache.http.HttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;

/**
 * WebSocketFrameHandler 继承自 SimpleChannelInboundHandler，用于处理WebSocket帧。
 */
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private LoaderConfig config = new LoaderConfig();

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
            String message = textWebSocketFrame.text();
            System.out.println("WebSocket Received Message: " + message);

            // Create Payload
            Payload payload = new Payload();
            payload.setModel(config.getModel());
            payload.setTemperature(config.getTemperature());
            payload.setMaxTokens(config.getMaxTokens());
            payload.setStream(true);
            String apiKeys = config.getApiKeys();
            String proxyUrl = config.getProxyUrl() + "/v1/chat/completions";
            payload.setMessages(Collections.singletonList(new Message("user", message)));

            HttpResponse httpResponse = HttpUtil.requestOpenAI(JsonUtil.toJson(payload), proxyUrl, apiKeys);

            try (InputStream inputStream = httpResponse.getEntity().getContent()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ") && !line.contains("[DONE]")) {
                        // 移除前缀，获取纯粹的JSON字符串
                        String json = line.substring("data: ".length());
                        String content = JsonUtil.getStreamContent(json);
                        //  为了前端渲染地更加自然，加了50ms的延迟
                        Thread.sleep(50);
                        // 立即将数据发送给WebSocket客户端
                        channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(content));
                    } else if (line.contains("[DONE]")){
                        String done = line.substring(line.indexOf(":") + 1).trim();
                        channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(done));
                    }
                }
            } catch (IOException e) {
                // 异常处理
                throw new RuntimeException("Error while reading the stream", e);
            }
        }
    }
}
