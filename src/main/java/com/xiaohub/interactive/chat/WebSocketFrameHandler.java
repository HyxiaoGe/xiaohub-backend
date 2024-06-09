package com.xiaohub.interactive.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.xiaohub.config.OpenAIConfig;
import com.xiaohub.interactive.model.Message;
import com.xiaohub.interactive.model.Payload;
import com.xiaohub.interactive.model.request.Content;
import com.xiaohub.interactive.model.request.ImageUrl;
import com.xiaohub.interactive.model.request.ImageContent;
import com.xiaohub.util.AESUtil;
import com.xiaohub.util.HttpUtil;
import com.xiaohub.util.JsonUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * WebSocketFrameHandler 继承自 SimpleChannelInboundHandler，用于处理WebSocket帧。
 */
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    public static final Logger log = LoggerFactory.getLogger(WebSocketFrameHandler.class);

    private OpenAIConfig config = new OpenAIConfig();

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
            JsonNode contentJson = JsonUtil.readObject(textWebSocketFrame.text());
            String action = contentJson.get("action").asText();
            if ("verify".equals(action)) {
                String secretKey = contentJson.get("secretKey").asText();
                boolean isVerified = validateKey(secretKey);
                if (isVerified) {
                    channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame("success"));
                } else {
                    channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame("failure"));
                }
            } else if ("session".equals(action)) {
                Payload payload = new Payload();
                payload.setModel(config.getModel());
                payload.setTemperature(config.getTemperature());
                payload.setMaxTokens(config.getMaxTokens());
                payload.setStream(true);
                String apiKeys = config.getApiKeys();
                String proxyUrl = config.getProxyUrl() + "/v1/chat/completions";
                List<Message> messages = parseContent(contentJson);
                payload.setMessages(messages);

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
                        } else if (line.contains("[DONE]")) {
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

    private List<Message> parseContent(JsonNode contentJson) {
        //  图片消息
        if (!contentJson.get("file").isNull()) {
            return populateMessagesWithImageUrl(contentJson);
        } else {
            // 文本消息
            String conversation = contentJson.get("conversation").toString();
            return JsonUtil.toObjectList(conversation, Message.class);
        }
    }

    private static List<Message> populateMessagesWithImageUrl(JsonNode contentJson) {
        String imgBase64Url = contentJson.get("file").asText();
        String conversation = contentJson.get("conversation").toString();
        List<Message> messages = JsonUtil.toObjectList(conversation, Message.class);
        // 获取最后一个role为user的Message
        Message message = null;
        for (int i = messages.size() - 1; i >= 0; i--) {
            if ("user".equals(messages.get(i).getRole())) {
                message = messages.get(i);
                break;
            }
        }
        ImageContent imageContent = new ImageContent();
        ImageUrl image_url = new ImageUrl();
        image_url.setUrl(imgBase64Url);
        imageContent.setImage_url(image_url);

        List<Content> contentList = message.getContent();
        contentList.add(imageContent);
        message.setContent(contentList);
        return messages;
    }

    /**
     * 密钥验证
     *
     * @param secretKey
     * @return
     * @throws Exception
     */
    private boolean validateKey(String secretKey) throws Exception {
        String key = config.getAESKey();
        String originSecretKey = config.getSecretKey();

        return originSecretKey.equals(AESUtil.encrypt(key, secretKey));
    }

}
