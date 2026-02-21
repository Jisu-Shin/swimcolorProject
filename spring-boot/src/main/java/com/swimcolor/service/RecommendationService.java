package com.swimcolor.service;

import com.swimcolor.client.ApiClient;
import com.swimcolor.domain.ColorMatch;
import com.swimcolor.domain.Swimcap;
import com.swimcolor.domain.ViewType;
import com.swimcolor.dto.RecommendListDto;
import com.swimcolor.dto.RecommendResponseDto;
import com.swimcolor.dto.SwimcapListDto;
import com.swimcolor.mapper.SwimcapMapper;
import com.swimcolor.repository.JpaColorMatchRepository;
import com.swimcolor.repository.JpaSwimcapRepository;
import com.swimcolor.repository.dto.ColorRecommendDto;
import com.swimcolor.util.ColorDifferenceCalculator;
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

    private final JpaColorMatchRepository colorMatchRepository;
    private final JpaSwimcapRepository swimcapRepository;
    private final SwimcapMapper swimcapMapper;

    private final double MIN_DISTANCE = 5.0;
    private final String ALGORITHM_VERSION = "2.0.0";

    public List<SwimcapListDto> recommendSwimcaps(String swimsuitId, List<String> colors) {
        // 1. SwimsuitCapSimilarity에 값이 있는지 확인하기
        List<ColorMatch> colorMatchList = colorMatchRepository.findBySwimsuitIdOrderBySimilarityScoreDesc(swimsuitId);

        // todo Exception으로 던지는걸로 해야하나?
        // 기존
        if (colors.isEmpty()) {
            return List.of();
        }
//        validateColors(colors);

        // 2-1. 크롤링 이후 or 색상 매칭에 값이 없는 경우는
        // 수모 추천은 fastapi를 호출하여 값을 가져오기
        if (recentViewLogService.isAfterCrawling(swimsuitId) || colorMatchList.isEmpty()) {
            log.info("컬러매치 데이터가 없거나, swimsuitId({}) 조회 날짜보다 이후에 크롤링 함 -> 외부호출", swimsuitId);

            // 3. fastapi 외부호출
//            RecommendResponseDto recommendResponseDto = apiClient.getRecommendSwimcap(swimsuitId, colors);
            // todo 이제 스프링에서 돌리는거라 getSwimcapListDtoList 어떻게 생략할 수 있을 거 같은데...
            List<RecommendListDto> similarList = getColorDifference(swimsuitId, colors);

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

    public void validateColors(List<String> colors) {
        if (colors.isEmpty()) {
            throw new IllegalStateException("수영복의 색상리스트가 없습니다.");
        }
    }

    @Nonnull
    private List<SwimcapListDto> getSwimcapListDtoList(List<String> swimcapIds) {
        List<Swimcap> swimcapList = swimcapRepository.findByIdsWithColors(swimcapIds);
        return swimcapList.stream()
                .map(s -> swimcapMapper.toDto(s))
                .collect(Collectors.toList());
    }

    private List<RecommendListDto> getColorDifference(String swimsuitId, List<String> colors) {
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
                .sorted(new Comparator<RecommendListDto>() {
                    @Override
                    public int compare(RecommendListDto o1, RecommendListDto o2) {
                        return Double.compare(o1.getSimilarityScore(), o2.getSimilarityScore());
                    }
                })
                .limit(6)
                .toList();

        return result;
    }
}
