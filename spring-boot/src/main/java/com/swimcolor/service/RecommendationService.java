package com.swimcolor.service;

import com.swimcolor.domain.ColorMatch;
import com.swimcolor.domain.Swimcap;
import com.swimcolor.domain.ViewType;
import com.swimcolor.dto.RecommendListDto;
import com.swimcolor.dto.SwimcapListDto;
import com.swimcolor.mapper.SwimcapMapper;
import com.swimcolor.repository.JpaColorMatchRepository;
import com.swimcolor.repository.JpaSwimcapRepository;
import com.swimcolor.service.recommendation.RecommendationAlgorithm;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final ColorMatchService colorMatchService;
    private final RecentViewLogService recentViewLogService;
    private final RecommendationAlgorithm recommendationAlgorithm;

    private final JpaColorMatchRepository colorMatchRepository;
    private final JpaSwimcapRepository swimcapRepository;
    private final SwimcapMapper swimcapMapper;

    public List<SwimcapListDto> recommendSwimcaps(String swimsuitId, List<String> colors) {
        if (validateColors(colors)) {
            return List.of();
        }

        // 1. SwimsuitCapSimilarity에 값이 있는지 확인하기
        List<ColorMatch> colorMatchList = colorMatchRepository.findBySwimsuitIdOrderBySimilarityScoreDesc(swimsuitId);

        // 2-1. 크롤링 이후 or 색상 매칭에 값이 없는 경우는
        // 수모 추천은 fastapi를 호출하여 값을 가져오기
        if (recentViewLogService.isAfterCrawling(swimsuitId) || colorMatchList.isEmpty()) {
            log.info("컬러매치 데이터가 없거나, swimsuitId({}) 조회 날짜보다 이후에 크롤링 함 -> 외부호출", swimsuitId);

            // todo 이제 스프링에서 돌리는거라 getSwimcapListDtoList 어떻게 생략할 수 있을 거 같은데...
            List<RecommendListDto> similarList = recommendationAlgorithm.recommend(swimsuitId, colors);

            // todo 추천데이터가 없는 경우
            if (similarList.isEmpty()) {
                return List.of();
            }

            // 4. 색상 매칭 데이터를 저장하고
            colorMatchService.saveColorMatch(similarList);
            List<String> swimcapIds = similarList.stream()
                    .map(c -> c.getSwimcapId())
                    .toList();

            // 5. 추천한 수영복 최근뷰로그에 날짜 저장하기
            recentViewLogService.save(swimsuitId, ViewType.SWIMSUIT);

            return getSwimcapListDtoList(swimcapIds);
        }

        // 2-2. 크롤링 이전 and 색상 매칭에 데이터가 있는 경우
        // DB에 있는 그대로 수모를 추천하기
        log.info("swimsuitId({}) 조회 날짜보다 이전에 크롤링 함 -> DB조회", swimsuitId);

        List<String> swimcapIds = colorMatchList.stream()
                .map(c -> c.getSwimcapId())
                .toList();

        return getSwimcapListDtoList(swimcapIds);
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
