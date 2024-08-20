package com.xiaohub.datadigger;

import com.xiaohub.constants.HttpResponseWrapper;
import com.xiaohub.constants.Platform;
import com.xiaohub.datadigger.dto.Article;
import com.xiaohub.properties.AWSProperties;
import com.xiaohub.util.HttpRequestUtil;
import com.xiaohub.util.JsonUtil;
import com.xiaohub.util.RedisUtil;
import com.xiaohub.util.ThreadPoolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class DataAgent {

    public static final Logger log = LoggerFactory.getLogger(DataAgent.class);

    private static final String ARTICLES_KEY_PREFIX = "articles_by_platform:";
    private static final String UPDATES_STATUS_KEY_PREFIX = "status_updates_by_platform";

    private static AWSProperties awsProperties = new AWSProperties();

    private static final List<String> PLATFORM_LIST = new ArrayList<>();

    private static ExecutorService executorService = Executors.newFixedThreadPool(5);


    static {
        PLATFORM_LIST.add(Platform.ZAOBAO.getPlatform());
        PLATFORM_LIST.add(Platform.OEEEE.getPlatform());
        PLATFORM_LIST.add(Platform.CHLINLEARN.getPlatform());
        PLATFORM_LIST.add(Platform.DEEPLEARNING.getPlatform());
        PLATFORM_LIST.add(Platform.KR_36.getPlatform());

        scheduleFetchTask();
    }

    public static void init() {
    }

    private static void scheduleFetchTask() {
        ThreadPoolUtil.scheduledAtFixedRate(DataAgent::fetch, 0, 60, TimeUnit.MINUTES);
    }

    public static void fetch() {
        List<Future<Boolean>> futures = new ArrayList<>();
        Map<String, Boolean> platformUpdates = new ConcurrentHashMap<>();

        for (String platform : PLATFORM_LIST) {
            futures.add(executorService.submit(() -> fetchDataForPlatform(platform, platformUpdates)));
        }

        for (Future<Boolean> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("Failed to complete fetching task", e);
            }
        }

        platformUpdates.forEach(DataAgent::updatePlatformStatus);
    }

    private static Boolean fetchDataForPlatform(String platform, Map<String, Boolean> platformUpdates) {
        log.info("{} fetch data...", platform);
        try {
            Map<String, String> params = new HashMap<>();
            params.put("platform", platform);
//            HttpResponseWrapper httpResponse = HttpRequestUtil.sendGetRequest("http://localhost:5000/articles", params);
            HttpResponseWrapper httpResponse = HttpRequestUtil.sendGetRequest(awsProperties.getDatadiggerUrl(), params);
            int code = httpResponse.getCode();
            if (code == 200) {
                List<Article> articles = JsonUtil.objectMapper.treeToValue(httpResponse.getJson(), JsonUtil.objectMapper.getTypeFactory().constructCollectionType(List.class, Article.class));
                boolean hasNewItem = RedisUtil.saveList(ARTICLES_KEY_PREFIX + platform, articles, Article.class);
                platformUpdates.put(platform, hasNewItem);

                return true;
            } else {
                log.warn("error code: {}, error msg: {}", httpResponse.getCode(), httpResponse.getJson().asText());
            }
        } catch (Exception e) {
            log.error("Exception fetching data for platform {}: {}", platform, e.getMessage());
        }
        platformUpdates.put(platform, false);

        return false;
    }

    private static void updatePlatformStatus(String platform, Boolean hasNewItem) {
        if (hasNewItem) {
            long currentTimeStamp = System.currentTimeMillis() / 1000;
            RedisUtil.setHashValue(UPDATES_STATUS_KEY_PREFIX, platform, String.valueOf(currentTimeStamp));
            log.info("Updated {} status in Redis with timestamp {}", platform, currentTimeStamp);
        }
    }

    public static Map<String, String> getAllPlatformStatus() {
        return RedisUtil.getHashAll(ARTICLES_KEY_PREFIX);
    }

    public static List<Article> retrieve(String platform) {
        int stop;
        if (platform.equals(Platform.KR_36.getPlatform())) {
            stop = 9;
        } else {
            stop = 19;
        }
        return RedisUtil.getList(ARTICLES_KEY_PREFIX + platform, Article.class, 0, stop);
    }

}
