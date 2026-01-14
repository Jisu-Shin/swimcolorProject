package com.swimcolor.controller.api;

import com.swimcolor.domain.FeedBackType;
import com.swimcolor.dto.ColorMatchFeedbackDto;
import com.swimcolor.service.ColorMatchFeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/colorMatchFeedback")
public class ColorMatchFeedbackApiController {
    private final ColorMatchFeedbackService colorMatchFeedbackService;

    @PostMapping("/errorModelExtract")
    public ResponseEntity<String> errorModelExtract(@Valid @RequestBody ColorMatchFeedbackDto requestDto) {
        requestDto.setReviewedAt(LocalDateTime.now());
        requestDto.setFeedBackType(FeedBackType.ERROR_MODEL_EXTRACT);
        colorMatchFeedbackService.save(requestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/errorColorRecommend")
    public ResponseEntity<String> errorColorRecommend(@Valid @RequestBody ColorMatchFeedbackDto requestDto) {
        requestDto.setReviewedAt(LocalDateTime.now());
        requestDto.setFeedBackType(FeedBackType.ERROR_COLOR_RECOMMEND);
        colorMatchFeedbackService.save(requestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
