package com.swimcolor.service;

import com.swimcolor.domain.CrawlStatus;
import com.swimcolor.dto.CrawlListDto;
import com.swimcolor.dto.CrawlResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * AdminService의 responseCrawlSwimsuits 메서드에 대한 Mockup 테스트
 */
@ExtendWith(MockitoExtension.class)
class AdminServiceMockupTest {

    @InjectMocks
    private AdminService adminService;

    @Mock
    private SwimsuitService swimsuitService;

    @Mock
    private CrawlStatusService crawlStatusService;

    @Mock
    private CrawlingLogService crawlingLogService;

    @Test
    @DisplayName("수영복 크롤링 성공 응답 처리 - COMPLETED 상태")
    void responseCrawlSwimsuits_Success() {
        // given
        Long logId = 1L;
        int expectedCount = 3;

        CrawlResponseDto successResponse = createSuccessResponse(logId, expectedCount);
        System.out.println(successResponse);

        // Mock 설정
        when(swimsuitService.saveSwimsuit(any(CrawlResponseDto.class)))
                .thenReturn(expectedCount);

        // when
        adminService.responseCrawlSwimsuits(successResponse);

        // then
        // 1. 크롤링 상태가 완료로 변경되었는지 확인
        verify(crawlStatusService, times(1)).completeSwimsuitCrawling();

        // 2. 수영복 저장 메서드가 호출되었는지 확인
        verify(swimsuitService, times(1)).saveSwimsuit(successResponse);

        // 3. 크롤링 로그가 COMPLETED로 업데이트 되었는지 확인
        verify(crawlingLogService, times(1))
                .updateCrawlingLog(eq(logId), eq(CrawlStatus.COMPLETED), eq(expectedCount), eq(null));

        // 4. 실패 관련 메서드가 호출되지 않았는지 확인
        verify(crawlStatusService, never()).failSwimsuitCrawling();
    }

    @Test
    @DisplayName("수영복 크롤링 실패 응답 처리 - FAILED 상태")
    void responseCrawlSwimsuits_Failed() {
        // given
        Long logId = 2L;
        String errorMessage = "크롤링 중 오류 발생";

        CrawlResponseDto failedResponse = createFailedResponse(logId, errorMessage);

        // when
        adminService.responseCrawlSwimsuits(failedResponse);

        // then
        // 1. 크롤링 상태가 실패로 변경되었는지 확인
        verify(crawlStatusService, times(1)).failSwimsuitCrawling();

        // 2. 크롤링 로그가 FAILED로 업데이트 되었는지 확인
        verify(crawlingLogService, times(1))
                .updateCrawlingLog(eq(logId), eq(CrawlStatus.FAILED), eq(0), eq(errorMessage));

        // 3. 성공 관련 메서드가 호출되지 않았는지 확인
        verify(crawlStatusService, never()).completeSwimsuitCrawling();
        verify(swimsuitService, never()).saveSwimsuit(any());
    }

    @Test
    @DisplayName("수영복 크롤링 성공 - 저장 건수 0건")
    void responseCrawlSwimsuits_Success_ZeroItems() {
        // given
        Long logId = 3L;
        int expectedCount = 0;

        CrawlResponseDto successResponse = createSuccessResponse(logId, 0);

        // Mock 설정
        when(swimsuitService.saveSwimsuit(any(CrawlResponseDto.class)))
                .thenReturn(expectedCount);

        // when
        adminService.responseCrawlSwimsuits(successResponse);

        // then
        // 1. 크롤링은 성공했지만 저장된 데이터가 없음
        verify(crawlStatusService, times(1)).completeSwimsuitCrawling();
        verify(swimsuitService, times(1)).saveSwimsuit(successResponse);
        verify(crawlingLogService, times(1))
                .updateCrawlingLog(eq(logId), eq(CrawlStatus.COMPLETED), eq(0), eq(null));
    }

    @Test
    @DisplayName("수영복 크롤링 성공 - 다수의 아이템 저장")
    void responseCrawlSwimsuits_Success_MultipleItems() {
        // given
        Long logId = 4L;
        int expectedCount = 50;

        CrawlResponseDto successResponse = createSuccessResponse(logId, expectedCount);

        // Mock 설정
        when(swimsuitService.saveSwimsuit(any(CrawlResponseDto.class)))
                .thenReturn(expectedCount);

        // when
        adminService.responseCrawlSwimsuits(successResponse);

        // then
        verify(crawlStatusService, times(1)).completeSwimsuitCrawling();
        verify(swimsuitService, times(1)).saveSwimsuit(successResponse);
        verify(crawlingLogService, times(1))
                .updateCrawlingLog(eq(logId), eq(CrawlStatus.COMPLETED), eq(expectedCount), eq(null));
    }

    @Test
    @DisplayName("수영복 크롤링 실패 - 상세 에러 메시지 포함")
    void responseCrawlSwimsuits_Failed_WithDetailedError() {
        // given
        Long logId = 5L;
        String detailedError = "타임아웃 발생: 대상 서버가 응답하지 않습니다";

        CrawlResponseDto failedResponse = createFailedResponse(logId, detailedError);

        // when
        adminService.responseCrawlSwimsuits(failedResponse);

        // then
        verify(crawlStatusService, times(1)).failSwimsuitCrawling();
        verify(crawlingLogService, times(1))
                .updateCrawlingLog(eq(logId), eq(CrawlStatus.FAILED), eq(0), eq(detailedError));
        verify(swimsuitService, never()).saveSwimsuit(any());
    }

    // ==================== Helper Methods ====================

    /**
     * 성공 응답 DTO 생성
     */
    private CrawlResponseDto createSuccessResponse(Long logId, int itemCount) {
        CrawlResponseDto response = new CrawlResponseDto();
        response.setLogId(logId);
        response.setCrawlStatus(CrawlStatus.COMPLETED);
        response.setErrorMsg(null);

        // 테스트 데이터 생성
        List<CrawlListDto> products = createMockProducts(itemCount);
        response.setProducts(products);

        return response;
    }

    /**
     * 실패 응답 DTO 생성
     */
    private CrawlResponseDto createFailedResponse(Long logId, String errorMessage) {
        CrawlResponseDto response = new CrawlResponseDto();
        response.setLogId(logId);
        response.setCrawlStatus(CrawlStatus.FAILED);
        response.setErrorMsg(errorMessage);
        response.setProducts(null);

        return response;
    }

    /**
     * Mock 제품 리스트 생성
     */
    private List<CrawlListDto> createMockProducts(int count) {
        if (count == 0) {
            return Arrays.asList();
        }

        // 실제로는 CrawlListDto의 생성자나 빌더를 사용해야 하지만
        // 예시로 간단히 표현
        CrawlListDto product1 = new CrawlListDto();
        CrawlListDto product2 = new CrawlListDto();
        CrawlListDto product3 = new CrawlListDto();

        return Arrays.asList(product1, product2, product3);
    }
}