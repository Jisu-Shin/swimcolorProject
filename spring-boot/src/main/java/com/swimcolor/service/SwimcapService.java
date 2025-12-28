package com.swimcolor.service;

import com.swimcolor.domain.Swimcap;
import com.swimcolor.dto.CrawlResponseDto;
import com.swimcolor.mapper.SwimcapMapper;
import com.swimcolor.repository.JpaSwimcapRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SwimcapService {
    private final JpaSwimcapRepository swimcapRepository;
    private final SwimcapMapper swimcapMapper;

    @Transactional
    public int saveSwimcap(CrawlResponseDto responseDto) {
        List<Swimcap> swimcapList = responseDto.getProducts().stream()
                .map(p->swimcapMapper.toEntity(p))
                .toList();
        swimcapRepository.saveAll(swimcapList);

        return swimcapList.size();
    }
}
