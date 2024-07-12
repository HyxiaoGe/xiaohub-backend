package com.xiaohub.interactive.image.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.xiaohub.config.OpenAIConfig;
import com.xiaohub.interactive.common.BasicMessage;
import com.xiaohub.interactive.image.dto.content.ImageContentDto;
import com.xiaohub.interactive.image.dto.payload.ImagePayloadDto;
import com.xiaohub.util.AESUtil;
import com.xiaohub.util.HttpUtil;
import com.xiaohub.util.JsonUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * WebSocketFrameHandler 继承自 SimpleChannelInboundHandler，用于处理WebSocket帧。
 */
public class ImageWebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    public static final Logger log = LoggerFactory.getLogger(ImageWebSocketFrameHandler.class);

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
                imagePayloadDto.setModel(config.getImageModel());
                imagePayloadDto.setN(Integer.parseInt(config.getAmount()));
                imagePayloadDto.setSize(config.getSize());
                imagePayloadDto.setPrompt(content);
                String apiKeys = config.getApiKeys();
                String proxyUrl = config.getProxyUrl() + "/v1/images/generations";
                String contentText;
                String revisedPrompt = "An image of a heartwarmingly cute baby sea otter. It's fiddling with a small shell in its tiny paws as it floats on its back in clear, tranquil seawater. The sunlight gently reflects off the water, enhancing the otter's thick, soft-looking fur, which ripples and shines with a wet gleam. It is small and compact, a tender look of curiosity in its round, gleaming eyes. All around it, shades of color depict the marine environment - blues and greens subtly blending together in a serenely beautiful oceanic scene.";
                String imgUrl = "https://oaidalleapiprodscus.blob.core.windows.net/private/org-Mp4gPyU9PhTX3P0EmFsYNYuC/user-yVGxOIePSlTR0UKt1nU2l0Pq/img-7jRYOuGnhGdm9qNYvE9efs5C.png?st=2024-07-11T12%3A24%3A59Z&se=2024-07-11T14%3A24%3A59Z&sp=r&sv=2023-11-03&sr=b&rscd=inline&rsct=image/png&skoid=6aaadede-4fb3-4698-a8f6-684d7786b067&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skt=2024-07-11T02%3A20%3A33Z&ske=2024-07-12T02%3A20%3A33Z&sks=b&skv=2023-11-03&sig=SiMS%2B5m4XrO2wnvdvdQzpanKn2vCLhH7dwlQZTpXUl0%3D";
                contentText = JsonUtil.objectMapper.writeValueAsString(new BasicMessage(0, "image", imgUrl));
                channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(contentText));
                for (int i = 0; i < revisedPrompt.length(); i++) {
                    String chunk = revisedPrompt.substring(i, Math.min(i + 1, revisedPrompt.length()));
                    contentText = JsonUtil.objectMapper.writeValueAsString(new BasicMessage(0, "text", chunk));
                    channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(contentText));
                    Thread.sleep(50);
                }
                contentText = JsonUtil.objectMapper.writeValueAsString(new BasicMessage(0, "text", "[DONE]"));
                channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(contentText));
//                HttpResponse httpResponse = HttpUtil.requestOpenAI(JsonUtil.toJson(imagePayloadDto), proxyUrl, apiKeys);
//                int statusCode = httpResponse.getStatusLine().getStatusCode();
//                if (statusCode == 200) {
//                    String jsonContent = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
//                    ImageContentDto imageContentDto = JsonUtil.objectMapper.readValue(jsonContent, ImageContentDto.class);
//                    if (!imageContentDto.getData().isEmpty()) {
//                        ImageContentDto.DataItem dataItem = imageContentDto.getData().get(0);
//                        String revisedPrompt = dataItem.getRevisedPrompt();
//                        String imgUrl = dataItem.getUrl();
//                        String contentText;
//                        contentText = JsonUtil.objectMapper.writeValueAsString(new BasicMessage(0, "image", imgUrl));
//                        channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(contentText));
//                        for (int i = 0; i < revisedPrompt.length(); i++) {
//                            String chunk = revisedPrompt.substring(i, Math.min(i + 1, revisedPrompt.length()));
//                            contentText = JsonUtil.objectMapper.writeValueAsString(new BasicMessage(0, "text", chunk));
//                            channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(contentText));
//                            Thread.sleep(50);
//                        }
//                        contentText = JsonUtil.objectMapper.writeValueAsString(new BasicMessage(0, "text", "[DONE]"));
//                        channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(contentText));
//                    }
//                } else if (statusCode == 429) {
//                    log.error("当前图片生成过快，请稍后重试！！！");
//                } else {
//                    String errorMessage = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
//                    log.error("Error: HTTP Status Code: {}", statusCode);
//                    log.error("Error Details: {}", errorMessage);
//                }
            }
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
        String key = config.getAESKey();
        String originSecretKey = config.getSecretKey();

        return originSecretKey.equals(AESUtil.encrypt(key, secretKey));
    }

}
