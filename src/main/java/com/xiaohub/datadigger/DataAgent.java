package com.xiaohub.datadigger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xiaohub.constants.HttpResponseWrapper;
import com.xiaohub.datadigger.dto.Article;
import com.xiaohub.util.HttpUtil;
import com.xiaohub.util.JsonUtil;
import com.xiaohub.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class DataAgent {

    public static final Logger log = LoggerFactory.getLogger(DataAgent.class);

    private static final String REDIS_KEY = "articles";


    public static void fetch() {

        HttpResponseWrapper httpResponse = HttpUtil.sendGetRequest("http://localhost:5000/articles", null);

        try {
            // 线程池定时拉取，还要设置异步（数据量过大）
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            List<Article> articles = JsonUtil.objectMapper.treeToValue(httpResponse.getJson(),
                    JsonUtil.objectMapper.getTypeFactory().constructCollectionType(List.class, Article.class));
            RedisUtil.saveList(REDIS_KEY, articles);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        log.info("data fetch complete!!!");
    }

    public static List<Article> retrieve() {
        return RedisUtil.getList(REDIS_KEY, Article.class, 0, 9);
    }

//    public static void main(String[] args) {
//        fetch();
//    }

}
