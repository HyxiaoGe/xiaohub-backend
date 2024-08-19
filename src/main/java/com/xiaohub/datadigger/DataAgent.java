package com.xiaohub.datadigger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xiaohub.constants.HttpResponseWrapper;
import com.xiaohub.constants.Platform;
import com.xiaohub.datadigger.dto.Article;
import com.xiaohub.interactive.insight.model.PlatformStatus;
import com.xiaohub.properties.AWSProperties;
import com.xiaohub.util.HttpRequestUtil;
import com.xiaohub.util.JsonUtil;
import com.xiaohub.util.RedisUtil;
import com.xiaohub.util.ThreadPoolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class DataAgent {

    public static final Logger log = LoggerFactory.getLogger(DataAgent.class);

    private static final String ARTICLES_KEY = "articles-";

    private static final String UPDATES_STATUS_KEY = "updates_status-";

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
                boolean hasNewItem = RedisUtil.saveList(ARTICLES_KEY + platform, articles, Article.class);
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
            long currentTime = System.currentTimeMillis() / 1000;
            Map<String, Object> status = new HashMap<>();
            status.put("timestamp", currentTime);
            status.put("updated", true);

            try {
                RedisUtil.setHashValue(UPDATES_STATUS_KEY, platform, JsonUtil.objectMapper.writeValueAsString(status));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            log.info("Updated {} status in Redis with timestamp {}", platform, currentTime);
        }
    }

    public static Map<String, PlatformStatus> getAllStatus() {
        // 从Redis获取所有平台的更新状态
        Map<String, String> statusMap = RedisUtil.getHashAll(UPDATES_STATUS_KEY);
        Map<String, PlatformStatus> platformStatusMap = new HashMap<>();

        for (Map.Entry<String, String> entry : statusMap.entrySet()) {
            try {
                PlatformStatus status = JsonUtil.objectMapper.readValue(entry.getValue(), PlatformStatus.class);
                platformStatusMap.put(entry.getKey(), status);
            } catch (IOException e) {
                log.error("Error parsing platform status for key {}: {}", entry.getKey(), e.getMessage());
            }
        }

        return platformStatusMap;
    }


    public static List<Article> retrieve(String platform) {
        int stop;
        if (platform.equals(Platform.KR_36.getPlatform())) {
            stop = 9;
        } else {
            stop = 19;
        }
        return RedisUtil.getList(ARTICLES_KEY + platform, Article.class, 0, stop);
    }

}
