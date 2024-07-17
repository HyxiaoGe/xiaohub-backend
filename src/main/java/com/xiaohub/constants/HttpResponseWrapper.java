package com.xiaohub.constants;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HttpResponseWrapper {

    private final HttpResponse response;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String responseBody;
    private JsonNode jsonNode;

    public HttpResponseWrapper(HttpResponse response) {
        this.response = response;
    }

    public int getCode() {
        return response.getStatusLine().getStatusCode();
    }

    public JsonNode getJson(){
        if (jsonNode == null) {
            try {
                responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                jsonNode = objectMapper.readTree(responseBody);
                return jsonNode;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

}
