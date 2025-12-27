package com.swimcolor.service;

import com.swimcolor.domain.Swimsuit;
import com.swimcolor.dto.CrawlResponseDto;
import com.swimcolor.dto.SwimsuitListDto;
import com.swimcolor.repository.JpaSwimsuitRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.swimcolor.mapper.SwimsuitMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SwimsuitService {
    private final JpaSwimsuitRepository swimsuitRepository;
    private final SwimsuitMapper swimsuitMapper;

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
}
