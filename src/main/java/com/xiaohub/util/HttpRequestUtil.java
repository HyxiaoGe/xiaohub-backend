package com.xiaohub.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xiaohub.constants.HttpResponseWrapper;
import com.xiaohub.exception.ConnectionTimeoutException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestUtil {

    private static final CloseableHttpClient httpClient = createClientAcceptsUntrustedCerts();

    public static final Logger log = LoggerFactory.getLogger(HttpRequestUtil.class);

    private static CloseableHttpClient createClientAcceptsUntrustedCerts() {
        try {
            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();

            // 配置请求的超时设置
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(60000).setSocketTimeout(60000).setConnectionRequestTimeout(60000).build();

//            HttpHost proxy = new HttpHost("127.0.0.1", 7890);
//            DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);

            return HttpClients.custom().setSSLContext(sslContext).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).setDefaultRequestConfig(requestConfig)
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

    public static HttpResponse proxyRequestOpenAI(String awsProxyUrl, String payload, String proxyUrl, String apiKeys) throws ConnectionTimeoutException {

        HttpPost httpPost = new HttpPost(awsProxyUrl);

        Map<String, String> requestData = new HashMap<>();
        requestData.put("payload", payload);
        requestData.put("proxyUrl", proxyUrl);
        requestData.put("apiKeys", apiKeys);

        String jsonString;
        try {
            jsonString = JsonUtil.objectMapper.writeValueAsString(requestData);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize requestData to JSON. requestData: {}", requestData, e);
            throw new RuntimeException("Error serializing requestData to JSON", e);
        }

        httpPost.addHeader("Content-Type", "application/json");
        StringEntity stringEntity = new StringEntity(jsonString, "UTF-8");
        httpPost.setEntity(stringEntity);
        try {
            return httpClient.execute(httpPost);
        } catch (SocketTimeoutException | HttpHostConnectException e) {
            log.error("Socket timeout occurred while executing HTTP request: {}", e.getMessage(), e);
            throw new ConnectionTimeoutException("当前请求超时，请稍后再试！！！", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static HttpResponseWrapper sendPostRequest(String url, Map<String, String> params, String contentType) {
        HttpPost httpPost = new HttpPost(url);

        switch (contentType) {
            case "application/json":
                String jsonString;
                try {
                    jsonString = JsonUtil.objectMapper.writeValueAsString("");
                } catch (JsonProcessingException e) {
                    log.error("Failed to serialize requestData to JSON. requestData: {}", "", e);
                    throw new RuntimeException("Error serializing requestData to JSON", e);
                }
                httpPost.addHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
                StringEntity stringEntity = new StringEntity(jsonString, "UTF-8");
                break;
            case "multipart/form-data":
                httpPost.setHeader("Content-Type", ContentType.MULTIPART_FORM_DATA.getMimeType());
                break;
            case "application/x-www-form-urlencoded":
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
                params.forEach((key, value) -> nameValuePairs.add(new BasicNameValuePair(key, value)));
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, StandardCharsets.UTF_8));
                httpPost.setHeader("Content-Type", ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
                break;
            default:
                throw new IllegalArgumentException("Unsupported content type: " + contentType);
        }
        try {
            HttpResponse response = httpClient.execute(httpPost);
            return new HttpResponseWrapper(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static HttpResponseWrapper sendGetRequest(String url, Map<String, String> params) {
        // 构建URL
        StringBuilder urlWithParams = new StringBuilder(url);
        if (params != null && !params.isEmpty()) {
            urlWithParams.append("?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                urlWithParams.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            urlWithParams.setLength(urlWithParams.length() - 1);
        }

        HttpGet httpGet = new HttpGet(urlWithParams.toString());

        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            return new HttpResponseWrapper(httpResponse);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
