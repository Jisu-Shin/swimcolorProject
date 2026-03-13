package com.swimcolor.service;

import com.swimcolor.domain.Swimcap;
import com.swimcolor.domain.ViewType;
import com.swimcolor.dto.RecommendListDto;
import com.swimcolor.dto.SwimcapListDto;
import com.swimcolor.mapper.SwimcapMapper;
import com.swimcolor.repository.JpaSwimcapRepository;
import com.swimcolor.service.recommendation.RecommendationAlgorithm;
import com.swimcolor.service.recommendation.RecommendationCacheService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class RecommendationService {
    private final ColorMatchService colorMatchService;
    private final RecentViewLogService recentViewLogService;
    private final RecommendationAlgorithm recommendationAlgorithm;
    private final RecommendationCacheService recommendationCacheService;

    private final JpaSwimcapRepository swimcapRepository;
    private final SwimcapMapper swimcapMapper;

    public List<SwimcapListDto> recommendSwimcaps(String swimsuitId, List<String> colors) {
        if (validateColors(colors)) {
            return List.of();
        }

        // 1. 캐시 조회
        List<String> cachedSwimcapIds = recommendationCacheService.getCachedRecommendation(swimsuitId);
        if (!cachedSwimcapIds.isEmpty()) {
            log.info("캐시 사용 (DB) ");
            return getSwimcapListDtoList(cachedSwimcapIds);
        }

        log.info("컬러매치 데이터가 없거나, swimsuitId({}) 조회 날짜보다 이후에 크롤링 함 -> 알고리즘 수행 ", swimsuitId);

        // 2. 알고리즘 수행
        // todo 이제 스프링에서 돌리는거라 getSwimcapListDtoList 어떻게 생략할 수 있을 거 같은데...
        List<RecommendListDto> similarList = recommendationAlgorithm.recommend(swimsuitId, colors);

        // todo 추천데이터가 없는 경우
        if (similarList.isEmpty()) {
            return List.of();
        }

        // 3. 색상 매칭 데이터를 저장
        List<String> swimcapIds = saveColorMatch(swimsuitId, similarList);
        return getSwimcapListDtoList(swimcapIds);
    }

    @Nonnull
    private List<String> saveColorMatch(String swimsuitId, List<RecommendListDto> similarList) {
        colorMatchService.saveColorMatch(similarList);
        List<String> swimcapIds = similarList.stream()
                .map(c -> c.getSwimcapId())
                .toList();

        // 5. 추천한 수영복 최근뷰로그에 날짜 저장하기
        recentViewLogService.save(swimsuitId, ViewType.SWIMSUIT);
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

    @Nonnull
    private List<SwimcapListDto> getSwimcapListDtoList(List<String> swimcapIds) {
        List<Swimcap> swimcapList = swimcapRepository.findByIdsWithColors(swimcapIds);
        return swimcapList.stream()
                .map(s -> swimcapMapper.toDto(s))
                .collect(Collectors.toList());
    }
}
