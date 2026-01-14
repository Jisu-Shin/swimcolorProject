package com.swimcolor.service;

import com.swimcolor.domain.ColorMatchFeedback;
import com.swimcolor.dto.ColorMatchFeedbackDto;
import com.swimcolor.mapper.ColorMatchFeedbackMapper;
import com.swimcolor.repository.JpaColorMatchFeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ColorMatchFeedbackService {
    private final JpaColorMatchFeedbackRepository colorMatchFeedbackRepository;
    private final ColorMatchFeedbackMapper colorMatchFeedbackMapper;

    public void save(ColorMatchFeedbackDto dto) {
        ColorMatchFeedback colorMatchFeedback = colorMatchFeedbackMapper.toEntity(dto);
        colorMatchFeedbackRepository.save(colorMatchFeedback);
    }
}
