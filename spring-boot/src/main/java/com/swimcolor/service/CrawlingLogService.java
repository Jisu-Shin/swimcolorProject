package com.swimcolor.service;

import com.swimcolor.domain.CrawlingLog;
import com.swimcolor.dto.CrawlingLogResponseDto;
import com.swimcolor.mapper.CrawlingLogMapper;
import com.swimcolor.repository.JpaCrawlingLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CrawlingLogService {

    private final JpaCrawlingLogRepository  jpaCrawlingLogRepository;
    private final CrawlingLogMapper crawlingLogMapper;

    @Transactional
    public void saveCrawlingLog(CrawlingLog crawlingLog){
        jpaCrawlingLogRepository.save(crawlingLog);
    }

    public List<CrawlingLogResponseDto> findAllCrawlingLog(){
        List<CrawlingLog> logList = jpaCrawlingLogRepository.findAllByOrderByCrawledAtDesc();
        return logList.stream()
                .map(l->crawlingLogMapper.toDto(l))
                .toList();
    }
}
