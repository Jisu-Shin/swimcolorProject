package com.swimcolor.service.recommendation;

import com.swimcolor.domain.ViewType;
import com.swimcolor.dto.RecommendListDto;
import com.swimcolor.dto.SwimcapListDto;
import com.swimcolor.service.ColorMatchService;
import com.swimcolor.service.RecentViewLogService;
import com.swimcolor.service.item.SwimcapService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class RecommendationService {
    private final ColorMatchService colorMatchService;
    private final RecentViewLogService recentViewLogService;
    private final RecommendationAlgorithm recommendationAlgorithm;
    private final RecommendationCacheService recommendationCacheService;
    private final SwimcapService swimcapService;

    public List<SwimcapListDto> recommendSwimcaps(String swimsuitId, List<String> colors) {
        // todo 글로벌예외핸들러 처리하면서 수정해야함 validate Exception 던지기
        if (validateColors(colors)) {
            return List.of();
        }

        // 1. 캐시 조회
        List<String> cachedSwimcapIds = recommendationCacheService.getCachedRecommendation(swimsuitId);
        if (!cachedSwimcapIds.isEmpty()) {
            log.info("캐시 사용 (DB) - swimsuitId={} ", swimsuitId);
            return swimcapService.findSwimcapsByIds(cachedSwimcapIds, swimsuitId);
        }

        log.info("추천 알고리즘 수행 - swimsuitId={} ", swimsuitId);

        // 2. 알고리즘 수행
        List<RecommendListDto> similarList = recommendationAlgorithm.recommend(swimsuitId, colors);
        if (similarList.isEmpty()) {
            return List.of();
        }

        // 3. 색상 매칭 데이터를 저장
        List<String> swimcapIds = saveColorMatch(similarList);

        // 4. 추천한 수영복 최근뷰로그에 날짜 저장하기
        recentViewLogService.save(swimsuitId, ViewType.SWIMSUIT);

        return swimcapService.findSwimcapsByIds(swimcapIds, swimsuitId);
    }

    @Nonnull
    private List<String> saveColorMatch(List<RecommendListDto> similarList) {
        colorMatchService.saveColorMatch(similarList);
        List<String> swimcapIds = similarList.stream()
                .map(c -> c.getSwimcapId())
                .toList();
        return swimcapIds;
    }

    public Boolean validateColors(List<String> colors) {
        if (colors.isEmpty()) {
            return true;
            // todo Exception으로 던지는걸로 해야하나?
//            throw new IllegalStateException("수영복의 색상리스트가 없습니다.");
        }
        return false;
    }
}
