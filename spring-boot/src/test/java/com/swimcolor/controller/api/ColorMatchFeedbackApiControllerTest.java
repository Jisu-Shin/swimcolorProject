package com.swimcolor.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swimcolor.dto.ColorMatchFeedbackDto;
import com.swimcolor.service.ColorMatchFeedbackService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ColorMatchFeedbackApiController.class)
@AutoConfigureMockMvc(addFilters = false) // 인증 필터 끄기
class ColorMatchFeedbackApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // Spring Boot Test에서 자동 주입 가능

    @MockitoBean
    private ColorMatchFeedbackService colorMatchFeedbackService;

    @Test
    @DisplayName("컬러매치 피드백 저장 - 정상 케이스")
    void errorModelExtract_Success() throws Exception {
        // given
        ColorMatchFeedbackDto requestDto = new ColorMatchFeedbackDto();
        requestDto.setColorMatchId(1L);
        requestDto.setAlgorithmVersion(1);
        requestDto.setReviewedBy("admin");

        doNothing().when(colorMatchFeedbackService).save(any(ColorMatchFeedbackDto.class));

        // when & then
        mockMvc.perform(post("/api/colorMatchFeedback/errorModelExtract")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(colorMatchFeedbackService, times(1)).save(any(ColorMatchFeedbackDto.class));
    }

    @Test
    @DisplayName("컬러매치 피드백 저장 - colorMatchId null (400 Bad Request)")
    void errorModelExtract_ColorMatchIdNull_BadRequest() throws Exception {
        // given
        ColorMatchFeedbackDto requestDto = new ColorMatchFeedbackDto();
        // colorMatchId 설정 안함 (null)
//        requestDto.setAlgorithmVersion(1);
//        requestDto.setReviewedBy("admin");

        // when & then
        mockMvc.perform(post("/api/colorMatchFeedback/errorModelExtract")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CMN-001"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));

        // Service 호출 안 됨
        verify(colorMatchFeedbackService, never()).save(any());
    }

}