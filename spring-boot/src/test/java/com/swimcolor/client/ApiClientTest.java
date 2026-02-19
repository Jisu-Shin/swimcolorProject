package com.swimcolor.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApiClientTest {

    @Autowired
    private ApiClient apiClient;

    @Test
    void 실제_람다_크롤링_호출_확인() throws InterruptedException {
        // given
        String testUrl = "https://swim.co.kr/categories/918698/products?childCategoryNo=919173&brands=%255B43160567%255D&pageNumber=1";
        Long logId = 1L;

        // when
        apiClient.crawlSwimsuits(testUrl, logId);

        // 비동기라서 Lambda가 호출될 시간을 잠깐 줌
        Thread.sleep(3000);

        // then - Lambda 콘솔 또는 CloudWatch에서 실행 확인
        System.out.println("Lambda 호출 완료 - AWS Lambda 콘솔에서 실행 로그 확인하세요!");
    }
}

