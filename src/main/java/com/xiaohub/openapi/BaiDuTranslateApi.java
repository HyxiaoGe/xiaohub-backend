package com.xiaohub.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.xiaohub.properties.BaiDuProperties;
import com.xiaohub.constants.ContentType;
import com.xiaohub.constants.HttpResponseWrapper;
import com.xiaohub.util.HttpUtil;
import com.xiaohub.util.MD5Util;
import com.xiaohub.util.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class BaiDuTranslateApi {

    public static final Logger log = LoggerFactory.getLogger(BaiDuTranslateApi.class);

    private static final String BAIDU_TRANSLATE_API = "https://fanyi-api.baidu.com/api/trans/vip/translate";

    private static final BaiDuProperties BAIDU_PROPERTIES = new BaiDuProperties();

    public static String translate(String queryText) {

        String appId = BAIDU_PROPERTIES.getAppId();
        String appSecret = BAIDU_PROPERTIES.getAppSecret();
        String salt = RandomGenerator.generateRandomNumber();
        String appendStr = appId + queryText + salt + appSecret;
        String sign = MD5Util.getMD5(appendStr);

        Map<String, String> params = Map.of(
                "q", queryText,
                "from", "en",
                "to", "zh",
                "appid", appId,
                "salt", salt,
                "sign", sign
        );

        HttpResponseWrapper response = HttpUtil.sendPostRequest(BAIDU_TRANSLATE_API, params, ContentType.URL_ENCODED.getMimeType());
        int code = response.getCode();

        String resultText = "";
        if (code == 200) {
            JsonNode jsonNode = response.getJson();
            log.info("jsonContent: {}", jsonNode);
            resultText = jsonNode.get("trans_result").get(0).get("dst").asText();
        } else {
            log.error("Error Code: {}", code);
        }
        return resultText;
    }

}
