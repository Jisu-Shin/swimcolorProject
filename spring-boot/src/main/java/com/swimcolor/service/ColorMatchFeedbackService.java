package com.swimcolor.service;

import com.swimcolor.domain.ColorMatchFeedback;
import com.swimcolor.dto.ColorMatchFeedbackDto;
import com.swimcolor.exception.ColorMatchException;
import com.swimcolor.exception.ErrorCode;
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
        boolean isExist = colorMatchFeedbackRepository.existsByColorMatchIdAndFeedbackType(dto.getColorMatchId(), dto.getFeedbackType());

        if (isExist) {
            throw new ColorMatchException(ErrorCode.COLOR_MATCH_FEEDBACK_ALREADY_EXISTS);
        }

        ColorMatchFeedback colorMatchFeedback = colorMatchFeedbackMapper.toEntity(dto);
        colorMatchFeedbackRepository.save(colorMatchFeedback);
    }
}
