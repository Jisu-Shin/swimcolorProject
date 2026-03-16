package com.swimcolor.service;

import com.swimcolor.domain.ItemType;
import com.swimcolor.domain.Swimcap;
import com.swimcolor.dto.CrawlResponseDto;
import com.swimcolor.dto.SwimcapListDto;
import com.swimcolor.mapper.SwimcapMapper;
import com.swimcolor.repository.JpaSwimcapRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SwimcapService implements ItemService {
    private final JpaSwimcapRepository swimcapRepository;
    private final SwimcapMapper swimcapMapper;

    @Override
    @Transactional
    public int save(CrawlResponseDto responseDto) {
        List<Swimcap> swimcapList = responseDto.getProducts().stream()
                .map(p->swimcapMapper.toEntity(p, responseDto.getLogId()))
                .toList();
        swimcapRepository.saveAll(swimcapList);

        return swimcapList.size();
    }

    @Override
    public ItemType getItemType() {
        return ItemType.SWIMCAP;
    }

    public List<SwimcapListDto> findSwimcapsByIds(List<String> swimcapIds) {
        // todo 알고리즘 버전
        List<Swimcap> swimcapList = swimcapRepository.findByIdsWithColors(swimcapIds, "2.0.1");
        return swimcapList.stream()
                .map(s -> swimcapMapper.toDto(s))
                .collect(Collectors.toList());
    }
}
