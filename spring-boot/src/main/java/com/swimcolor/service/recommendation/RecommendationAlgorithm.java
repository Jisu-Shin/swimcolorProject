package com.swimcolor.service.recommendation;

import com.swimcolor.dto.RecommendListDto;

import java.util.List;

public interface RecommendationAlgorithm {
    List<RecommendListDto> recommend(
            String swimsuitId,
            List<String> colors
    );
}
