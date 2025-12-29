package com.swimcolor.service;

import com.swimcolor.domain.ColorMatch;
import com.swimcolor.dto.RecommendListDto;
import com.swimcolor.dto.RecommendResponseDto;
import com.swimcolor.mapper.ColorMatchMapper;
import com.swimcolor.repository.JpaColorMatchRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ColorMatchService {

    private final JpaColorMatchRepository colorMatchRepository;
    private final ColorMatchMapper colorMatchMapper;

    @Transactional
    public int saveColorMatch(List<RecommendListDto> similarList) {
        List<ColorMatch> colorMatchList = similarList
                .stream()
                .map(s-> colorMatchMapper.toEntity(s))
                .toList();
        colorMatchRepository.saveAll(colorMatchList);

        return colorMatchList.size();
    }
}
