package com.xiaohub.interactive.chat.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.xiaohub.config.OpenAIConfig;
import com.xiaohub.interactive.chat.dto.message.TextMessageDto;
import com.xiaohub.interactive.chat.dto.message.TextPayloadDto;
import com.xiaohub.interactive.chat.dto.message.BasicMessageDto;
import com.xiaohub.interactive.chat.dto.content.ContentDto;
import com.xiaohub.interactive.chat.dto.content.ImageUrl;
import com.xiaohub.interactive.chat.dto.content.ImageContentDtoDto;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * WebSocketFrameHandler 继承自 SimpleChannelInboundHandler，用于处理WebSocket帧。
 */
public class ChatWebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    public static final Logger log = LoggerFactory.getLogger(ChatWebSocketFrameHandler.class);

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
            String content = "";
            Integer sessionId = -1;
            BasicMessageDto basicMessageDto = new BasicMessageDto(sessionId, content);
            if ("verify".equals(action)) {
                String secretKey = contentJson.get("secretKey").asText();
                boolean isVerified = validateKey(secretKey);
                if (isVerified) {
                    basicMessageDto.setContent("success");
                    String rspContent = JsonUtil.toJson(basicMessageDto);
                    channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(rspContent));
                } else {
                    basicMessageDto.setContent("failure");
                    String rspContent = JsonUtil.toJson(basicMessageDto);
                    channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(rspContent));
                }
            } else if ("session".equals(action)) {
                TextPayloadDto textPayloadDto = new TextPayloadDto();
                textPayloadDto.setModel(config.getModel());
                textPayloadDto.setTemperature(config.getTemperature());
                textPayloadDto.setMaxTokens(config.getMaxTokens());
                textPayloadDto.setStream(true);
                String apiKeys = config.getApiKeys();
                String proxyUrl = config.getProxyUrl() + "/v1/chat/completions";
                List<TextMessageDto> textMessageDtos;
                try {
                    textMessageDtos = parseContent(contentJson);
                } catch (Exception e) {
                    channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame("系统当前繁忙，请稍后重试！！！"));
                    throw new RuntimeException(e);
                }
                textPayloadDto.setMessages(textMessageDtos);
                HttpResponse httpResponse = HttpUtil.requestOpenAI(JsonUtil.toJson(textPayloadDto), proxyUrl, apiKeys);
                try (InputStream inputStream = httpResponse.getEntity().getContent()) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ") && !line.contains("[DONE]")) {
                            // 移除前缀，获取纯粹的JSON字符串
                            String json = line.substring("data: ".length());
                            sessionId = contentJson.get("sessionId").asInt();
                            content = JsonUtil.getStreamContent(json);
                            //  为了前端渲染地更加自然，加了50ms的延迟
                            Thread.sleep(50);
                            // 立即将数据发送给WebSocket客户端
                            basicMessageDto = new BasicMessageDto(sessionId, content);
                            String rspContent = JsonUtil.toJson(basicMessageDto);
                            channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(rspContent));
                        } else if (line.contains("[DONE]")) {
                            String done = line.substring(line.indexOf(":") + 1).trim();
                            basicMessageDto = new BasicMessageDto(sessionId, done);
                            String rspContent = JsonUtil.toJson(basicMessageDto);
                            channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(rspContent));
                        }
                    }
                } catch (Exception e) {
                    // 异常处理
                    throw new RuntimeException("Error while reading the stream", e);
                }
            }
        }
    }

    private List<TextMessageDto> parseContent(JsonNode contentJson) {
        //  图片消息
        if (!contentJson.get("file").isNull()) {
            return populateMessagesWithImageUrl(contentJson);
        } else {
            // 文本消息
            String conversation = contentJson.get("conversation").toString();
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
        ImageContentDtoDto imageContentDto = new ImageContentDtoDto();
        ImageUrl image_url = new ImageUrl();
        image_url.setUrl(imgBase64Url);
        imageContentDto.setImage_url(image_url);

        List<ContentDto> contentDtoList = textMessageDto.getContent();
        contentDtoList.add(imageContentDto);
        textMessageDto.setContent(contentDtoList);
        return textMessageDtos;
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
