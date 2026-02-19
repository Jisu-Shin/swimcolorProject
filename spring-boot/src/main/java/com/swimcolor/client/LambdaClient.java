package com.swimcolor.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swimcolor.dto.CrawlResponseDto;
import com.swimcolor.dto.RecommendResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaAsyncClient;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Primary
@Service
@RequiredArgsConstructor
@Slf4j
public class LambdaClient implements ApiClient{
    private final LambdaAsyncClient lambdaAsyncClient;
    private final String FUNCTION_NAME = "lambda-crawling";

    @Value("${crawling.callbackUrl}")
    private String crawlingCallbackUrl;

    public void invokeLambda(String functionName, String payload) {
        log.info("functionName:{}, payload:{}", functionName, payload);

        InvokeRequest request = InvokeRequest.builder()
                .functionName(functionName)
                .invocationType(InvocationType.EVENT) // 비동기
                .payload(SdkBytes.fromUtf8String(payload))
                .build();

        lambdaAsyncClient.invoke(request)
                .exceptionally(e -> {
                    log.error("Lambda 호출 실패 - functionName: {}, error: {}", functionName, e.getMessage());
                    return null;
                });
    }

    @Override
    public void crawlSwimsuits(String url, Long logId) {
        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, String> payloadMap = new HashMap<>();
        payloadMap.put("logId", logId.toString());
        payloadMap.put("url", url);
        payloadMap.put("callbackUrl", crawlingCallbackUrl.concat("/swimsuits"));

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payloadMap);
        } catch (JsonProcessingException e) {
            log.error("Lambda payload 직렬화 실패 - logId: {}, error: {}", logId, e.getMessage());
            return;
        }

        invokeLambda(FUNCTION_NAME, payloadJson);
    }

    @Override
    public CrawlResponseDto crawlSwimcaps(String url) {
        return null;
    }

    @Override
    public RecommendResponseDto getRecommendSwimcap(String swimsuitId, List<String> colors) {
        return null;
    }
}
