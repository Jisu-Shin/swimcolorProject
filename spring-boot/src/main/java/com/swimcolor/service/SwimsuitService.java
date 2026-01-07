package com.swimcolor.service;

import com.swimcolor.domain.Swimsuit;
import com.swimcolor.dto.CrawlResponseDto;
import com.swimcolor.dto.SwimsuitListDto;
import com.swimcolor.repository.JpaSwimsuitRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.swimcolor.mapper.SwimsuitMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SwimsuitService {
    private static final int PAGE_SIZE = 12;
    private static final int POPULAR_SIZE = 4;
    private final JpaSwimsuitRepository swimsuitRepository;
    private final SwimsuitMapper swimsuitMapper;

    @Transactional
    public int saveSwimsuit(CrawlResponseDto responseDto) {
        List<Swimsuit> swimsuitList = responseDto.getProducts().stream()
                .map(s -> swimsuitMapper.toEntity(s, responseDto.getLogId()))
                .toList();
        swimsuitRepository.saveAll(swimsuitList);

        return swimsuitList.size();
    }

    public List<SwimsuitListDto> getPopularSwimsuit() {
        return swimsuitRepository.findAll().stream()
                .limit(POPULAR_SIZE)
                .map(s -> swimsuitMapper.toDto(s))
                .collect(Collectors.toList());
    }

    public List<SwimsuitListDto> getAllSwimsuit() {
        return swimsuitRepository.findAll().stream()
                .map(s -> swimsuitMapper.toDto(s))
                .collect(Collectors.toList());
    }

    public Page<SwimsuitListDto> getSwimsuitList(int page) {
        // 최근 글이 먼저 오도록 정렬 (0페이지부터 시작함에 주의!)
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").descending());

        Page<Swimsuit> swimsuitPage = swimsuitRepository.findAll(pageable);

        // Page 객체가 제공하는 map()을 사용해 DTO로 변환합
        // 이렇게 하면 페이징 정보(현재 페이지, 전체 페이지 등)는 유지되면서 내용물만 DTO로 바뀝니다.
        return swimsuitPage.map(swimsuit -> swimsuitMapper.toDto(swimsuit));
    }

    public SwimsuitListDto getSwimsuit(String id) {
        return swimsuitRepository.findById(id)
                .map(s -> swimsuitMapper.toDto(s))
                .orElse(null);
    }
}
