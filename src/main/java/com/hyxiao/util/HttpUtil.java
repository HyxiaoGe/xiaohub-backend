package com.hyxiao.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;

public class HttpUtil {

    // 创建一个忽略所有证书的 HttpClient 实例
    private static final CloseableHttpClient httpClient = createClientAcceptsUntrustedCerts();

    private static CloseableHttpClient createClientAcceptsUntrustedCerts() {
        try {
            // 信任自签名和所有主机名的策略
            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();

            return HttpClients.custom().setSSLContext(sslContext).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE) // 关闭主机名验证
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
