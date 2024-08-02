package com.xiaohub.interactive.image.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.xiaohub.properties.AWSProperties;
import com.xiaohub.properties.OpenAIProperties;
import com.xiaohub.exception.ConnectionTimeoutException;
import com.xiaohub.interactive.common.model.BasicMessage;
import com.xiaohub.interactive.image.dto.content.ImageContentDto;
import com.xiaohub.interactive.image.dto.payload.ImagePayloadDto;
import com.xiaohub.openapi.BaiDuTranslateApi;
import com.xiaohub.util.AESUtil;
import com.xiaohub.util.HttpRequestUtil;
import com.xiaohub.util.JsonUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * WebSocketFrameHandler 继承自 SimpleChannelInboundHandler，用于处理WebSocket帧。
 */
public class ImageWebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    public static final Logger log = LoggerFactory.getLogger(ImageWebSocketFrameHandler.class);

    private OpenAIProperties openAIProperties = new OpenAIProperties();

    private AWSProperties awsProperties = new AWSProperties();

    //  用于存储所有活动的WebSocket连接
    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        channels.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        channels.add(ctx.channel());
    }

    /**
     * 处理从客户端接收的每一个WebSocket帧
     *
     * @param channelHandlerContext
     * @param webSocketFrame
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, WebSocketFrame webSocketFrame) throws Exception {
        // 判断接收的是否为文本帧
        if (webSocketFrame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textWebSocketFrame = (TextWebSocketFrame) webSocketFrame;
            if ("ping".equals(textWebSocketFrame.text())) {
//                log.info("ImageServer: Received ping from the client");
                channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(JsonUtil.objectMapper.writeValueAsString(new BasicMessage(0, "heartbeat", "pong"))));
                return;
            }
            JsonNode contentJson = JsonUtil.readObject(textWebSocketFrame.text());
            String action = contentJson.get("action").asText();
            String content = contentJson.get("conversation").findValue("content").asText();
            if ("verify".equals(action)) {
                String secretKey = contentJson.get("secretKey").asText();
                boolean isVerified = validateKey(secretKey);
                if (isVerified) {
                    channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame("success"));
                } else {
                    channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame("failure"));
                }
            } else if ("session".equals(action)) {
                ImagePayloadDto imagePayloadDto = new ImagePayloadDto();
                imagePayloadDto.setModel(openAIProperties.getImageModel());
                imagePayloadDto.setN(Integer.parseInt(openAIProperties.getAmount()));
                imagePayloadDto.setSize(openAIProperties.getSize());
                imagePayloadDto.setPrompt(content);
                String apiKeys = openAIProperties.getApiKeys();
                String proxyUrl = openAIProperties.getProxyUrl() + "/v1/images/generations";

                HttpResponse httpResponse;
                try {
                    httpResponse = HttpRequestUtil.proxyRequestOpenAI(awsProperties.getProxyUrl(), JsonUtil.toJson(imagePayloadDto), proxyUrl, apiKeys);
                } catch (ConnectionTimeoutException e) {
                    sendMessage(channelHandlerContext, "errMsg", e.getMessage(), false);
                    return;
                }
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    String jsonContent = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
                    ImageContentDto imageContentDto = JsonUtil.objectMapper.readValue(jsonContent, ImageContentDto.class);
                    if (!imageContentDto.getData().isEmpty()) {
                        ImageContentDto.DataItem dataItem = imageContentDto.getData().get(0);
                        String revisedPrompt = dataItem.getRevisedPrompt();
                        String imgUrl = dataItem.getUrl();
                        sendMessage(channelHandlerContext, "image", imgUrl, false);
                        String resultText = BaiDuTranslateApi.translate(revisedPrompt);
                        sendMessage(channelHandlerContext, "text", resultText, true);
                    }
                } else {
                    String responseEntity = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                    String errMsg = JsonUtil.readObject(responseEntity).get("error").asText();
                    if (statusCode == 429 || ("Request failed with status code 429").equals(errMsg)) {
                        sendMessage(channelHandlerContext, "text", "当前图片生成过快，请稍后重试！！！", true);
                    } else if (statusCode == HttpStatus.SC_BAD_REQUEST && "content_policy_violation".equals(JsonUtil.readObject(responseEntity).get("error").get("code").asText())) {
                        sendMessage(channelHandlerContext, "text", "当前图片生成失败，OpenAI 不支持当前图片生成！！！", true);
                    } else {
                        log.error("Error: HTTP Status Code: {}", statusCode);
                        log.error("Error Details: {}", errMsg);
                    }
                }
            }
        }
    }

    private void sendMessage(ChannelHandlerContext ctx, String type, String message, boolean splitByChar) {
        try {
            if (splitByChar) {
                for (int i = 0; i < message.length(); i++) {
                    String chunk = message.substring(i, Math.min(i + 1, message.length()));
                    String contentText = JsonUtil.objectMapper.writeValueAsString(new BasicMessage(0, type, chunk));
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(contentText));
                    Thread.sleep(50);
                }
                String doneMessage = JsonUtil.objectMapper.writeValueAsString(new BasicMessage(0, "text", "[DONE]"));
                ctx.channel().writeAndFlush(new TextWebSocketFrame(doneMessage));
            } else {
                String contentText = JsonUtil.objectMapper.writeValueAsString(new BasicMessage(0, type, message));
                ctx.channel().writeAndFlush(new TextWebSocketFrame(contentText));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 密钥验证
     *
     * @param secretKey
     * @return
     * @throws Exception
     */
    private boolean validateKey(String secretKey) throws Exception {
        String key = openAIProperties.getAESKey();
        String originSecretKey = openAIProperties.getSecretKey();

        return originSecretKey.equals(AESUtil.encrypt(key, secretKey));
    }

}
