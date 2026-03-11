package com.swimcolor.service.recommendation;

import com.swimcolor.dto.RecommendListDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WeightedColorRecommendationAlgorithm implements RecommendationAlgorithm {
    @Override
    public List<RecommendListDto> recommend(String swimsuitId, List<String> colors) {
        return List.of();
    }
}
