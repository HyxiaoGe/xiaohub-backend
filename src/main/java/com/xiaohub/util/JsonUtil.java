package com.xiaohub.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class JsonUtil {

    public static final ObjectMapper objectMapper = new ObjectMapper();

    public static JsonNode readObject(String str) throws JsonProcessingException {
        return JsonUtil.objectMapper.readTree(str);
    }

    /**
     * 将对象转换为JSON字符串
     *
     * @param obj 转换对象
     * @return JSON字符串
     */
    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("对象转换为JSON字符串时发生错误", e);
        }
    }

    /**
     * 将JSON字符串转换为对象
     *
     * @param json  JSON字符串
     * @param clazz 对象类型
     * @param <T>   对象类型
     * @return 对象
     */
    // 用于处理单个对象
    public static <T> T toObject(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON字符串转换为对象时发生错误", e);
        }
    }

    // 用于处理对象列表
    public static <T> List<T> toObjectList(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON字符串转换为对象列表时发生错误", e);
        }
    }

    public static String getStreamContent(String jsonResponse) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        JsonNode choices = jsonNode.path("choices");
        String content = "";
        if (!choices.isEmpty() && choices.isArray()) {
            for (JsonNode choice : choices) {
                JsonNode delta = choice.findValue("delta");
                if (!delta.isEmpty()) {
                    content = delta.get("content").asText();
                }
            }
        }
        return content;
    }

}
