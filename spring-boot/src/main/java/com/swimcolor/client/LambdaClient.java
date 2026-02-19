package com.swimcolor.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swimcolor.dto.CrawlRequestDto;
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

import java.util.List;

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
        InvokeRequest request = InvokeRequest.builder()
                .functionName(functionName)
                .invocationType(InvocationType.EVENT) // 비동기
                .payload(SdkBytes.fromUtf8String(payload))
                .build();

        lambdaAsyncClient.invoke(request)
                .exceptionally(e -> {
                    log.error("Lambda 호출 실패: {}", e.getMessage());
                    throw new RuntimeException(e);
                });
    }

    @Override
    public void crawlSwimsuits(String url, Long logId) {
        ObjectMapper objectMapper = new ObjectMapper();

        CrawlRequestDto crawlRequestDto = CrawlRequestDto.builder()
                .logId(logId)
                .crawlingUrl(url)
                .callbackUrl(crawlingCallbackUrl.concat("/swimsuit"))
                .build();

        // todo 체크예외 해결하기
        String payloadJson = null;
        try {
            payloadJson = objectMapper.writeValueAsString(crawlRequestDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
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
