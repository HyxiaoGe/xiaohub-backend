package com.xiaohub.interactive.chat.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.xiaohub.config.AWSConfig;
import com.xiaohub.config.OpenAIConfig;
import com.xiaohub.exception.SensitiveWordException;
import com.xiaohub.interactive.chat.dto.content.ChatContentDto;
import com.xiaohub.interactive.chat.dto.content.ImageChatContentDto;
import com.xiaohub.interactive.chat.dto.content.ImageUrl;
import com.xiaohub.interactive.chat.dto.content.TextChatContentDto;
import com.xiaohub.interactive.chat.dto.message.TextMessageDto;
import com.xiaohub.interactive.chat.dto.message.TextPayloadDto;
import com.xiaohub.interactive.common.BasicMessage;
import com.xiaohub.util.AESUtil;
import com.xiaohub.util.HttpUtil;
import com.xiaohub.util.JsonUtil;
import com.xiaohub.util.SensitiveWordUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * WebSocketFrameHandler 继承自 SimpleChannelInboundHandler，用于处理WebSocket帧。
 */
public class ChatWebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    public static final Logger log = LoggerFactory.getLogger(ChatWebSocketFrameHandler.class);

    private OpenAIConfig openAIConfig = new OpenAIConfig();

    private AWSConfig awsConfig = new AWSConfig();

    private static final int ERROE_CODE = 99999;


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
                log.info("ChatServer: Received ping from the client");
                sendWebsocketResponse(channelHandlerContext, -1, "pong");
                return;
            }
            JsonNode contentJson = JsonUtil.readObject(textWebSocketFrame.text());
            String action = contentJson.get("action").asText();
            switch (action) {
                case "verify":
                    handleVerifyAction(channelHandlerContext, contentJson);
                    break;
                case "session":
                    handleSessionAction(channelHandlerContext, contentJson);
                    break;
                default:
                    log.warn("Unsupported action: {}", action);
            }
        }
    }

    private void handleVerifyAction(ChannelHandlerContext context, JsonNode contentJson) {
        String secretKey = contentJson.get("secretKey").asText();
        boolean isVerified = validateKey(secretKey);
        sendWebsocketResponse(context, -1, isVerified ? "success" : "failure");
    }

    private void handleSessionAction(ChannelHandlerContext context, JsonNode contentJson) throws IOException {
        TextPayloadDto textPayloadDto = new TextPayloadDto();
        textPayloadDto.setModel(openAIConfig.getChatModel());
        textPayloadDto.setTemperature(openAIConfig.getTemperature());
        textPayloadDto.setMaxTokens(openAIConfig.getMaxTokens());
        textPayloadDto.setStream(true);
        String apiKeys = openAIConfig.getApiKeys();
        String proxyUrl = openAIConfig.getProxyUrl() + "/v1/chat/completions";
        List<TextMessageDto> textMessageDtos;
        try {
            textMessageDtos = parseContent(contentJson);
        } catch (SensitiveWordException e) {
            sendWebsocketResponse(context, ERROE_CODE, e.getMessage());
            return;
        } catch (Exception e) {
            sendWebsocketResponse(context, ERROE_CODE, "系统当前繁忙，请稍后重试！！！");
            throw new RuntimeException(e);
        }
        textPayloadDto.setMessages(textMessageDtos);
        HttpResponse httpResponse = HttpUtil.proxyRequestOpenAI(awsConfig.getProxyUrl(), JsonUtil.toJson(textPayloadDto), proxyUrl, apiKeys);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (statusCode == HttpResponseStatus.OK.code()) {
            try (InputStream inputStream = httpResponse.getEntity().getContent()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                int sessionId = -1;
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ") && !line.contains("[DONE]")) {
                         // 移除前缀，获取纯粹的JSON字符串
                        String json = line.substring("data: ".length());
                        sessionId = contentJson.get("sessionId").asInt();
                        String content = JsonUtil.getStreamContent(json);
                        // 为了前端渲染地更加自然，加了50ms的延迟
                        Thread.sleep(50);
                        log.info("content:{}", content);
                        sendWebsocketResponse(context, sessionId, content);
                    } else if (line.contains("[DONE]")) {
                        String done = line.substring(line.indexOf(":") + 1).trim();
                        sendWebsocketResponse(context, sessionId, done);
                    }
                }
            } catch (Exception e) {
                 // 异常处理
                throw new RuntimeException("Error while reading the stream", e);
            }
        } else if (statusCode == HttpResponseStatus.FORBIDDEN.code()) {
            try {
                String jsonContent  = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
                String errMsg = JsonUtil.readObject(jsonContent).get("error").get("message").asText();
                log.error("请求失败，原因为：{}", errMsg);
                sendWebsocketResponse(context, ERROE_CODE, "当前服务暂时不可用，请稍后再试！！！");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            String errMsg  = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
            log.error("Error: HTTP Status Code: {}", statusCode);
            log.error("请求失败，原因为：{}", errMsg);
        }
    }

    private void sendWebsocketResponse(ChannelHandlerContext context, int sessionId, String content) {
        BasicMessage basicMessage = new BasicMessage(sessionId, "text", content);
        String rspContent = JsonUtil.toJson(basicMessage);
        context.channel().writeAndFlush(new TextWebSocketFrame(rspContent));
    }

    private List<TextMessageDto> parseContent(JsonNode contentJson) {
        //  图片消息
        if (!contentJson.get("file").isNull()) {
            return populateMessagesWithImageUrl(contentJson);
        } else {
            // 文本消息
            String conversation = contentJson.get("conversation").toString();
            List<TextMessageDto> messages = JsonUtil.toObjectList(conversation, TextMessageDto.class);
            String lastUserMessage = messages.stream()
                    .filter(message -> "user".equals(message.getRole()))
                    .map(TextMessageDto::getContent)
                    .flatMap(List::stream)
                    .filter(content -> content instanceof TextChatContentDto)
                    .map(content -> (TextChatContentDto) content)
                    .reduce((first, second) -> second)
                    .map(TextChatContentDto::getText)
                    .get();
            if (SensitiveWordUtil.isSensitiveWord(lastUserMessage)) {
                throw new SensitiveWordException("您的输入包含敏感词，请重新输入！！！");
            }
            return JsonUtil.toObjectList(conversation, TextMessageDto.class);
        }
    }

    private static List<TextMessageDto> populateMessagesWithImageUrl(JsonNode contentJson) {
        String imgBase64Url = contentJson.get("file").asText();
        String conversation = contentJson.get("conversation").toString();
        List<TextMessageDto> textMessageDtos = JsonUtil.toObjectList(conversation, TextMessageDto.class);
        // 获取最后一个role为user的Message
        TextMessageDto textMessageDto = null;
        for (int i = textMessageDtos.size() - 1; i >= 0; i--) {
            if ("user".equals(textMessageDtos.get(i).getRole())) {
                textMessageDto = textMessageDtos.get(i);
                break;
            }
        }
        ImageChatContentDto imageContentDto = new ImageChatContentDto();
        ImageUrl image_url = new ImageUrl();
        image_url.setUrl(imgBase64Url);
        imageContentDto.setImage_url(image_url);

        List<ChatContentDto> chatContentDtoList = textMessageDto.getContent();
        String lastUserMessage = ((TextChatContentDto) chatContentDtoList.get(0)).getText();
        if (SensitiveWordUtil.isSensitiveWord(lastUserMessage)) {
            throw new SensitiveWordException("您的输入包含敏感词，请重新输入！！！");
        }
        chatContentDtoList.add(imageContentDto);
        textMessageDto.setContent(chatContentDtoList);
        return textMessageDtos;
    }

    /**
     * 密钥验证
     *
     * @param secretKey
     * @return
     */
    private boolean validateKey(String secretKey) {
        try {
            String key = openAIConfig.getAESKey();
            String originSecretKey = openAIConfig.getSecretKey();
            return originSecretKey.equals(AESUtil.encrypt(key, secretKey));
        } catch (Exception e) {
            log.error("Key validation failed", e);
            return false;
        }
    }

    private void handlePingRequest() {

    }

}
