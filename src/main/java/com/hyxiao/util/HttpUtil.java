package com.hyxiao.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;

public class HttpUtil {

    private static final CloseableHttpClient httpClient;

    static {
        // 创建连接池管理器
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100); //  设置最大连接数
        connectionManager.setDefaultMaxPerRoute(10);    //  设置每个路由的默认最大连接数
        // 设置连接保持活跃的策略
        ConnectionKeepAliveStrategy keepAliveStrategy = (response, context) -> 60000;   // 保持活跃60秒
        // 设置请求配置
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30 * 1000).setConnectionRequestTimeout(30 * 1000).setSocketTimeout(60 * 1000).build();

        httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setKeepAliveStrategy(keepAliveStrategy)
                .setDefaultRequestConfig(requestConfig)
                .setRetryHandler((exception, executionCount, context) -> executionCount <= 3)
                .build();
    }

    public static HttpResponse requestOpenAI(String payload, String proxyUrl, String apiKeys) throws Exception {
        //  创建请求
        HttpPost httpPost = new HttpPost(proxyUrl);
        httpPost.addHeader("Content-Type", "application/json");
        httpPost.addHeader("Authorization", "Bearer " + apiKeys);
        //  设置请求体
        StringEntity stringEntity = new StringEntity(payload, "UTF-8");
        stringEntity.setContentType(new BasicHeader("Content-Type", "application/json"));
        httpPost.setEntity(stringEntity);
        //  发送请求并返回响应

        return httpClient.execute(httpPost);
    }

}
