package com.swimcolor.service.recommendation;

import com.swimcolor.dto.RecommendListDto;
import com.swimcolor.repository.JpaSwimcapRepository;
import com.swimcolor.repository.dto.ColorRecommendDto;
import com.swimcolor.util.ColorDifferenceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Primary
@RequiredArgsConstructor
@Component
public class BasicColorRecommendationAlgorithm implements RecommendationAlgorithm {
    private final JpaSwimcapRepository swimcapRepository;

    private final double MIN_DISTANCE = 5.0;
    private final String ALGORITHM_VERSION = "2.0.0";

    @Override
    public List<RecommendListDto> recommend(String swimsuitId, List<String> colors) {
        List<ColorRecommendDto> allColors = swimcapRepository.findAllColors();
        Map<String, RecommendListDto> similarMap = new HashMap<>();

        for (String suitColor : colors) {
            for (ColorRecommendDto cap : allColors) {
                double distance = ColorDifferenceCalculator.deltaE2000(suitColor, cap.getColor());

                if (distance < MIN_DISTANCE) {
                    RecommendListDto isExistDto = similarMap.get(cap.getSwimcapId());
                    RecommendListDto recommendListDto = new RecommendListDto(swimsuitId, suitColor, cap.getSwimcapId(), cap.getColor(), distance, ALGORITHM_VERSION);
                    if (isExistDto == null || distance < isExistDto.getSimilarityScore()) {
                        similarMap.put(cap.getSwimcapId(), recommendListDto);
                    }
                }
            }
        }

        List<RecommendListDto> result = similarMap.values().stream()
                .sorted(Comparator.comparingDouble(RecommendListDto::getSimilarityScore))
                .limit(6)
                .toList();

        return result;
    }
}
