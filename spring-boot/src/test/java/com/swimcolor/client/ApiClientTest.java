package com.swimcolor.client;

import com.swimcolor.dto.CrawlResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@SpringBootTest
class ApiClientTest {

    @Autowired
    private ApiClient apiClient;

    @Test
    void 외부호출_크롤링_정상동작_확인() {
        // given
        String testUrl = "https://swim.co.kr/categories/918698/products?childCategoryNo=919173&brands=%255B43160579%255D&pageNumber=1&categoryNos=%255B%255D";

        // when
        CrawlResponseDto response = apiClient.crawlSwimsuits(testUrl);

        // then
        System.out.println("response = " + response);

        assertThat(response).isNotNull();
        assertThat(response.getProducts()).isNotNull();

    }
}
