package com.tencent.wxcloudrun.service.impl;

/**
 * @author mayuzhou
 * @classname ChatGPTClient
 * @description TODO
 * @date 2023/4/5 12:48 上午
 */
import okhttp3.*;
import org.springframework.stereotype.Component;

import static java.awt.SystemColor.text;

@Component
public class ChatGPTClient {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = "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";
    private static final MediaType JSON_STRING = MediaType.get("application/json; charset=utf-8");

    private OkHttpClient client;

    public ChatGPTClient() {
        client = new OkHttpClient();
    }


    public String getCompletion(String content) throws Exception {
        // 构造请求的JSON数据
        String json = String.format("{\n" +
                                    "  \"model\": \"gpt-3.5-turbo\",\n" +
                                    "  \"messages\": [\n" +
                                    "    {\"role\": \"user\", \"content\": \"%s\"}\n" +
                                    "  ]\n" +
                                    "}", content);

        // 构造请求体
        RequestBody requestBody = RequestBody.create(json, JSON_STRING);

        // 构造请求
        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .post(requestBody)
                .build();

        // 发送请求
        Response response = client.newCall(request).execute();

        // 处理响应
        if (response.isSuccessful()) {
            String responseBody = response.body().string();
            System.out.println(responseBody);
        } else {
            String errorBody = response.body().string();
            System.out.println("HTTP " + response.code() + ": " + errorBody);
        }
//        JSONObject data = JSON.parseObject(text);
        return "";
    }
}