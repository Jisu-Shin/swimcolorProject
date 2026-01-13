package com.swimcolor.service;

import com.swimcolor.domain.ColorMatch;
import com.swimcolor.dto.ColorMatchDto;
import com.swimcolor.dto.ColorMatchListDto;
import com.swimcolor.dto.ColorMatchViewDto;
import com.swimcolor.dto.RecommendListDto;
import com.swimcolor.mapper.ColorMatchMapper;
import com.swimcolor.repository.JpaColorMatchRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ColorMatchService {

    private final JpaColorMatchRepository colorMatchRepository;
    private final ColorMatchMapper colorMatchMapper;

    @Transactional
    public int saveColorMatch(List<RecommendListDto> similarList) {
        String swimsuitId = similarList.get(0).getSwimsuitId();
        int algorithmVersion = similarList.get(0).getAlgorithmVersion();
        colorMatchRepository.deleteBulkBySwimsuitId(swimsuitId, algorithmVersion);

        List<ColorMatch> colorMatchList = similarList
                .stream()
                .map(colorMatchMapper::toEntity)
                .toList();
        colorMatchRepository.saveAll(colorMatchList);

        return colorMatchList.size();
    }

    public Page<ColorMatchDto> getColorMatchList(Pageable pageable) {
        Page<ColorMatchViewDto> colorMatchPage = colorMatchRepository.findColorMatches(pageable);

        // 2. 수영복 ID별로 그룹핑
        Map<String, List<ColorMatchViewDto>> groupedBySwimsuitId = colorMatchPage.stream()
                .collect(Collectors.groupingBy(
                        ColorMatchViewDto::getSwimsuitId,
                        LinkedHashMap::new, // 순서 유지
                        Collectors.toList()
                ));

        // 3. 그룹핑된 데이터를 최종 DTO로 변환
        List<ColorMatchDto> content = groupedBySwimsuitId.entrySet().stream()
                .map(entry -> convertToColorMatchDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        // 4. [핵심] 다시 Page 객체로 감싸서 반환 (전체 데이터 수 등 페이징 정보 유지)
        return new PageImpl<>(content, pageable, colorMatchPage.getTotalElements());
    }

    /**
     * ColorMatchViewDto 리스트를 ColorMatchDto로 변환
     */
    private ColorMatchDto convertToColorMatchDto(String swimsuitId, List<ColorMatchViewDto> viewDtos) {
        if (viewDtos.isEmpty()) {
            throw new IllegalStateException("ColorMatchViewDto list is empty for swimsuitId: " + swimsuitId);
        }

        // 첫 번째 항목에서 수영복 정보 추출 (모두 동일한 수영복 정보)
        ColorMatchViewDto first = viewDtos.get(0);

        // 수모 매칭 상세 리스트 생성
        List<ColorMatchListDto> detailList = viewDtos.stream()
                .map(colorMatchMapper::toDto)
                .collect(Collectors.toList());

        return new ColorMatchDto(
                first.getSwimsuitId(),
                first.getSwimsuitName(),
                first.getSwimsuitImageUrl(),
                detailList
        );
    }
}
