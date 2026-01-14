package com.swimcolor.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swimcolor.domain.CrawlingLog;
import com.swimcolor.service.CrawlStatusService;
import com.swimcolor.service.CrawlingLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CrawlingApiController.class)
@AutoConfigureMockMvc(addFilters = false) // 인증 필터 끄기
class CrawlingApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // Spring Boot Test에서 자동 주입 가능

    @MockitoBean
    private CrawlStatusService crawlStatusService;

    @MockitoBean
    private CrawlingLogService crawlingLogService;

    @Test
    public void 관리자크롤링취소요청() throws Exception {//given

        CrawlingLog crawlingLog = CrawlingLog.builder()
                .id(5L)
                .sourceUrl("https://~")
                .crawledAt(LocalDateTime.now())
                .build();
        when(crawlingLogService.getLastSwimcapCrawlingLog(any())).thenReturn(crawlingLog);

        //when
        mockMvc.perform(delete("/api/crawling/status/SWIMSUIT")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        )
                .andDo(print())
                .andExpect(status().is2xxSuccessful());

        //then
    }

}