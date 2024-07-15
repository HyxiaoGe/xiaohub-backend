package com.xiaohub.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

public class HttpUtil {

    private static final CloseableHttpClient httpClient = createClientAcceptsUntrustedCerts();

    public static final Logger log = LoggerFactory.getLogger(HttpUtil.class);

    private static CloseableHttpClient createClientAcceptsUntrustedCerts() {
        try {
            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();

            // 配置请求的超时设置
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30000).setSocketTimeout(30000).setConnectionRequestTimeout(30000).build();

//            HttpHost proxy = new HttpHost("127.0.0.1", 7890);
//            DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);

            return HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .setDefaultRequestConfig(requestConfig)
//                    .setRoutePlanner(routePlanner)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static HttpResponse directRequestOpenAI(String payload, String proxyUrl, String apiKeys) {

        int attemptCount = 0;
        int maxAttempt = 3;
        while (attemptCount <= maxAttempt) {
            try {
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
            } catch (SocketTimeoutException e) {
                if (attemptCount < maxAttempt) {
                    attemptCount++;
                    log.warn("请求超时，正在重试， 当前重试次数:{}/{}", attemptCount, maxAttempt);
                } else {
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }

    public static HttpResponse proxyRequestOpenAI(String awsProxyUrl, String payload, String proxyUrl, String apiKeys) throws JsonProcessingException {

        HttpPost httpPost = new HttpPost(awsProxyUrl);

        Map<String, String> requestData = new HashMap<>();
        requestData.put("payload", payload);
        requestData.put("proxyUrl", proxyUrl);
        requestData.put("apiKeys", apiKeys);

        String jsonString = JsonUtil.objectMapper.writeValueAsString(requestData);

        httpPost.addHeader("Content-Type", "application/json");
        StringEntity stringEntity = new StringEntity(jsonString, "UTF-8");
        httpPost.setEntity(stringEntity);
        try {
            return httpClient.execute(httpPost);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
