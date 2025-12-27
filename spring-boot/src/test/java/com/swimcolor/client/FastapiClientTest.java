package com.swimcolor.client;

import com.swimcolor.dto.CrawlResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
//@ActiveProfiles("test")
class FastapiClientTest {

    @Autowired
    private FastapiClient fastapiClient;

    @Test
    void fastapi크롤링_정상동작_확인() {
        // given
        String testUrl = "https://swim.co.kr/categories/918698/products?childCategoryNo=919173&brands=%255B43160579%255D&pageNumber=1&categoryNos=%255B%255D";

        // when
        CrawlResponseDto response = fastapiClient.crawlProducts(testUrl);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getProducts()).isNotNull();
        // 추가적으로 응답 값 구조 따라 구체적으로 검증 가능

        System.out.println("response = " + response);
    }
}
