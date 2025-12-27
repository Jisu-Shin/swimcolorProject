package com.swimcolor.service;

import com.swimcolor.domain.Swimsuit;
import com.swimcolor.domain.ColorMatch;
import com.swimcolor.domain.Swimcap;
import com.swimcolor.client.FastapiClient;
import com.swimcolor.dto.CrawlResponseDto;
import com.swimcolor.dto.SwimcapListDto;
import com.swimcolor.dto.SwimsuitListDto;
import com.swimcolor.mapper.SwimcapMapper;
import com.swimcolor.repository.JpaColorMatchRepository;
import com.swimcolor.repository.JpaSwimsuitRepository;
import com.swimcolor.repository.JpaSwimcapRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.swimcolor.mapper.SwimsuitMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SwimsuitService {
    private final JpaSwimsuitRepository swimsuitRepository;
    private final JpaSwimcapRepository swimcapRepository;
    private final JpaColorMatchRepository swimsuitCapSimilarityRepository;
    private final SwimsuitMapper swimsuitMapper;
    private final SwimcapMapper swimcapMapper;
    private final FastapiClient fastapiClient;

    @Transactional
    public int saveSwimsuit(CrawlResponseDto responseDto) {
        List<Swimsuit> swimsuitList = swimsuitMapper.toEntity(responseDto);
        swimsuitRepository.saveAll(swimsuitList);

        return swimsuitList.size();
    }

    public List<SwimsuitListDto> getPopularSwimsuit() {
        return swimsuitRepository.findAll().stream()
                .limit(4)
                .map(s -> swimsuitMapper.toDto(s))
                .collect(Collectors.toList());
    }

    public List<SwimsuitListDto> getAllSwimsuit() {
        return swimsuitRepository.findAll().stream()
                .map(s -> swimsuitMapper.toDto(s))
                .collect(Collectors.toList());
    }

    public SwimsuitListDto getSwimsuit(String id) {
        return swimsuitRepository.findById(id)
                .map(s -> swimsuitMapper.toDto(s))
                .orElse(null);
    }

    public List<SwimcapListDto> recommendSwimcapsBySwimsuitSimilarity(String id) {
        // 1. SwimsuitCapSimilarity에 값이 있는지 확인하기
        List<ColorMatch> colorMatchList = swimsuitCapSimilarityRepository.findBySwimsuitIdOrderBySimilarityDesc(id);

        // 2-1. 있는 경우 그대로 수모를 추천하기
        if (!colorMatchList.isEmpty()) {
            List<String> swimcapIds = colorMatchList.stream()
                    .map(c -> c.getSwimcapId())
                    .collect(Collectors.toList());
            List<Swimcap> swimcapList = swimcapRepository.findByIdIn(swimcapIds);
            return swimcapList.stream()
                    .map(s -> swimcapMapper.toDto(s))
                    .collect(Collectors.toList());
        }

        // 2-2. 없는 경우 fastapi를 호출하여 값을 가져오기
        // TODO: fastapiClient에서 수모 추천 결과를 받아오는 메서드 호출
        // 예: List<ColorMatch> newColorMatchList = fastapiClient.getSwimcapRecommendations(id);
        List<ColorMatch> newColorMatchList = new ArrayList<>(); // 임시 코드

        // 3. 리턴받은 값을 SwimsuitCapSimilarity에 저장하고
        if (!newColorMatchList.isEmpty()) {
            swimsuitCapSimilarityRepository.saveAll(newColorMatchList);
        }

        // 4. 수모를 리턴하기
        return newColorMatchList.stream()
                .map(colorMatch -> {
                    Swimcap swimcap = swimcapRepository.findById(colorMatch.getSwimcapId())
                            .orElse(null);
                    if (swimcap == null) {
                        return null;
                    }
                    SwimcapListDto dto = new SwimcapListDto();
                    dto.setName(swimcap.getName());
                    dto.setImageUrl(swimcap.getImageUrl());
                    dto.setProductUrl(swimcap.getProductUrl());
                    dto.setBrand(swimcap.getBrand());
                    dto.setPrice(swimcap.getPrice());
                    return dto;
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }
}
