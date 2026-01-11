package com.swimcolor.service;

import com.swimcolor.domain.CrawlStatus;
import com.swimcolor.domain.CrawlingLog;
import com.swimcolor.domain.ItemType;
import com.swimcolor.dto.CrawlingLogResponseDto;
import com.swimcolor.mapper.CrawlingLogMapper;
import com.swimcolor.repository.JpaCrawlingLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CrawlingLogService {

    private final JpaCrawlingLogRepository  jpaCrawlingLogRepository;
    private final CrawlingLogMapper crawlingLogMapper;

    @Transactional
    public Long saveCrawlingLog(CrawlingLog crawlingLog){
        return jpaCrawlingLogRepository.save(crawlingLog).getId();
    }

    public List<CrawlingLogResponseDto> findAllCrawlingLog(){
        List<CrawlingLog> logList = jpaCrawlingLogRepository.findAllByOrderByCrawledAtDesc();
        return logList.stream()
                .map(l->crawlingLogMapper.toDto(l))
                .toList();
    }

    // 크롤링요청한 id를 통해 엔티티 조회 후 데이터 수정
    @Transactional
    public void updateCrawlingLog(Long id, CrawlStatus crawlStatus, int count, String errMsg){
        CrawlingLog crawlingLog = jpaCrawlingLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("크롤링 로그가 없습니다 id: " + id));

        Long duration = Duration.between(crawlingLog.getCrawledAt(), LocalDateTime.now()).toMillis();

        crawlingLog.update(crawlStatus, count, errMsg, duration);
    }

    public CrawlingLog getLastSwimcapCrawlingLog(ItemType itemType){
        return jpaCrawlingLogRepository.findLastLogByItemType(itemType).stream()
                .findFirst().orElse(null);
    }
}
