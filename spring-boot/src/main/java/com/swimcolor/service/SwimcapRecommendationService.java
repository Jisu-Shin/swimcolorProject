package com.swimcolor.service;

import com.swimcolor.client.FastapiClient;
import com.swimcolor.domain.ColorMatch;
import com.swimcolor.domain.Swimcap;
import com.swimcolor.dto.RecommendResponseDto;
import com.swimcolor.dto.SwimcapListDto;
import com.swimcolor.mapper.SwimcapMapper;
import com.swimcolor.repository.JpaColorMatchRepository;
import com.swimcolor.repository.JpaSwimcapRepository;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SwimcapRecommendationService {
    private final FastapiClient fastapiClient;
    private final ColorMatchService colorMatchService;
    private final JpaColorMatchRepository colorMatchRepository;
    private final JpaSwimcapRepository swimcapRepository;
    private final SwimcapMapper swimcapMapper;

    public List<SwimcapListDto> recommendSwimcaps(String id) {
        // 1. SwimsuitCapSimilarity에 값이 있는지 확인하기
        List<ColorMatch> colorMatchList = colorMatchRepository.findBySwimsuitIdOrderBySimilarityDesc(id);

        // 2-1. 있는 경우 그대로 수모를 추천하기
        if (!colorMatchList.isEmpty()) {
            List<String> swimcapIds = colorMatchList.stream()
                    .map(c -> c.getSwimcapId())
                    .toList();

            return getSwimcapListDtoList(swimcapIds);
        }

        // 2-2. 없는 경우 fastapi를 호출하여 값을 가져오기
        RecommendResponseDto similarSwimCap = fastapiClient.getRecommendSwimcap(id, null);

        // 3. 리턴받은 값을 저장하고
        if (!similarSwimCap.getSimilarList().isEmpty()) {
            colorMatchService.saveColorMatch(similarSwimCap);
        }
        List<String> swimcapIds = similarSwimCap.getSimilarList().stream()
                .map(c->c.getSwimcapId())
                .toList();

        // 4. 수모를 리턴하기
        return getSwimcapListDtoList(swimcapIds);
    }

    @Nonnull
    private List<SwimcapListDto> getSwimcapListDtoList(List<String> swimcapIds) {
        List<Swimcap> swimcapList = swimcapRepository.findByIdIn(swimcapIds);
        return swimcapList.stream()
                .map(s -> swimcapMapper.toDto(s))
                .collect(Collectors.toList());
    }
}
