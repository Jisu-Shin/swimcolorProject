package com.swimcolor.service.recommendation;

import com.swimcolor.domain.ColorMatch;
import com.swimcolor.repository.JpaColorMatchRepository;
import com.swimcolor.service.RecentViewLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class RecommendationCacheService {

    private final JpaColorMatchRepository colorMatchRepository;
    private final RecentViewLogService recentViewLogService;

    public List<String> getCachedRecommendation(String swimsuitId) {

        // 1. SwimsuitCapSimilarity에 값이 있는지 확인하기
        // todo 알고리즘 버전
        List<ColorMatch> colorMatchList = colorMatchRepository.findBySwimsuitIdAndAlgorithmVersionOrderBySimilarityScore(swimsuitId, "2.0.1");

        // 컬러매치에 데이터가 있고, 크롤링을 조회시점보다 과거에 한 경우
        if (hasColorMatchList(colorMatchList) && recentViewLogService.isBeforeCrawling(swimsuitId)){
            return colorMatchList.stream()
                    .map(c -> c.getSwimcapId())
                    .toList();
        }

        return List.of();
    }

    private boolean hasColorMatchList(List<ColorMatch> colorMatchList) {
        return !colorMatchList.isEmpty();
    }
}
