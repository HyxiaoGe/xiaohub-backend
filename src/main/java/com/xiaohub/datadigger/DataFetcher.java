package com.xiaohub.datadigger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xiaohub.constants.HttpResponseWrapper;
import com.xiaohub.datadigger.dto.Article;
import com.xiaohub.util.HttpUtil;
import com.xiaohub.util.JsonUtil;
import com.xiaohub.util.RedisUtil;

import java.util.List;

public class DataFetcher {

    private static final String REDIS_KEY = "articles";

    public static void fetch() {

        HttpResponseWrapper httpResponse = HttpUtil.sendGetRequest("http://localhost:5000/articles", null);

        try {
            List<Article> articles = JsonUtil.objectMapper.treeToValue(httpResponse.getJson(),
                    JsonUtil.objectMapper.getTypeFactory().constructCollectionType(List.class, Article.class));
            RedisUtil.saveList(REDIS_KEY, articles);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

}
