package com.swimcolor.controller.api;

import com.swimcolor.domain.CrawlingLog;
import com.swimcolor.service.CrawlingService;
import com.swimcolor.service.CrawlingStateManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CrawlingApiController.class)
@AutoConfigureMockMvc(addFilters = false) // 인증 필터 끄기
class CrawlingApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CrawlingStateManager crawlingStateManager;

    @MockitoBean
    private CrawlingService crawlingService;

    @Test
    public void 관리자크롤링취소요청() throws Exception {
        //given
        CrawlingLog crawlingLog = CrawlingLog.builder()
                .id(5L)
                .sourceUrl("https://~")
                .crawledAt(LocalDateTime.now())
                .build();

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