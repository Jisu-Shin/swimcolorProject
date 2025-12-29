package com.swimcolor.service;

import com.swimcolor.client.FastapiClient;
import com.swimcolor.dto.SwimcapListDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RecommendationServiceTest {
    @Autowired
    RecommendationService recommendationService;

    @Test
    public void 수모추천서비스() throws Exception {
        //given
        String swimsuitId = "SS-0001";
        List<String> colors = List.of("#e3a1ca","#be76a1","#5b2b39");

        //when
        List<SwimcapListDto> swimcapListDtos = recommendationService.recommendSwimcaps(swimsuitId, colors);

        //then
        assertThat(swimcapListDtos).isNotEmpty();
    }
}