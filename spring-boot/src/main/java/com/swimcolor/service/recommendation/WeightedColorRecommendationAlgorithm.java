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
public class WeightedColorRecommendationAlgorithm implements RecommendationAlgorithm {
    private final JpaSwimcapRepository swimcapRepository;
    private final double[] WEIGHTS = {0.6, 0.3, 0.1};
    private final String ALGORITHM_VERSION = "2.0.1";

    @Override
    public List<RecommendListDto> recommend(String swimsuitId, List<String> colors) {
        List<ColorRecommendDto> allColors = swimcapRepository.findAllColors();

        Map<String, RecommendListDto> scoreMap = new HashMap<>();

        for (ColorRecommendDto cap : allColors) {

            double score = 0;

            for (int i = 0; i < colors.size(); i++) {
                String suitColor = colors.get(i);
                double distance = ColorDifferenceCalculator.deltaE2000(
                        suitColor,
                        cap.getColor()
                );

                score += WEIGHTS[i] * distance;
            }

            RecommendListDto recommendListDto = new RecommendListDto(swimsuitId, colors.get(0), cap.getSwimcapId(), cap.getColor(), score, ALGORITHM_VERSION);

            scoreMap.merge(
                    cap.getSwimcapId(),
                    recommendListDto,
                    (oldValue, newValue) ->
                            oldValue.getSimilarityScore() < newValue.getSimilarityScore()
                                    ? oldValue
                                    : newValue
            );
        }

        return scoreMap.values()
                .stream()
                .sorted(Comparator.comparingDouble(RecommendListDto::getSimilarityScore))
                .limit(6)
                .toList();
    }
}
