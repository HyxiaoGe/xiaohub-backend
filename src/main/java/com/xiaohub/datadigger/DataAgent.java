package com.xiaohub.datadigger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xiaohub.constants.HttpResponseWrapper;
import com.xiaohub.constants.Platform;
import com.xiaohub.datadigger.dto.Article;
import com.xiaohub.util.HttpUtil;
import com.xiaohub.util.JsonUtil;
import com.xiaohub.util.RedisUtil;
import com.xiaohub.util.ThreadPoolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DataAgent {

    public static final Logger log = LoggerFactory.getLogger(DataAgent.class);

    private static final String REDIS_KEY = "articles-";

    private static final List<String> PLATFORM_LIST = new ArrayList<>();

    static {
        PLATFORM_LIST.add(Platform.KR_36.getPlatform());
        PLATFORM_LIST.add(Platform.CHAPING.getPlatform());
        PLATFORM_LIST.add(Platform.ALIRESEARCH.getPlatform());

        scheduleFetchTask();
    }

    public static void init(){}

    private static void scheduleFetchTask() {
        ScheduledFuture<?> future = ThreadPoolUtil.scheduledAtFixedRate(DataAgent::fetch, 0, 60, TimeUnit.MINUTES);
        boolean done = future.isDone();
        if (done) {
            try {
                future.get(); // 只有在任务完成、取消或异常终止时返回
            } catch (ExecutionException e) {
                Throwable cause = e.getCause(); // 这里可以捕获到任务执行中抛出的异常
                log.error("Error during scheduled execution", cause);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void fetch() {
        log.info("fetch data");
        for (String platform : PLATFORM_LIST) {
            Map<String, String> params = new HashMap<>();
            params.put("platform", platform);
            HttpResponseWrapper httpResponse = HttpUtil.sendGetRequest("http://localhost:5000/articles", params);

            try {
                // 线程池定时拉取，还要设置异步（数据量过大）
                List<Article> articles = JsonUtil.objectMapper.treeToValue(httpResponse.getJson(), JsonUtil.objectMapper.getTypeFactory().constructCollectionType(List.class, Article.class));
                RedisUtil.saveList(REDIS_KEY + platform, articles);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            log.info("data fetch complete!!!");
        }
    }

    public static List<Article> retrieve(String platform) {
        return RedisUtil.getList(REDIS_KEY + platform, Article.class, 0, 9);
    }

}
