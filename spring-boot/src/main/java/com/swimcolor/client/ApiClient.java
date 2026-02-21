package com.swimcolor.client;

import com.swimcolor.domain.ItemType;
import com.swimcolor.dto.CrawlResponseDto;
import com.swimcolor.dto.RecommendResponseDto;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ApiClient {
    default public CrawlResponseDto crawlSwimsuits(String url) {
        return null;
    }

    public CrawlResponseDto crawlSwimcaps(String url);

    public RecommendResponseDto getRecommendSwimcap(String swimsuitId, List<String> colors);

    default public Mono<Void> crawlSwimsuitsAsync(String url, Long logId) {
        return null;
    }

    default public Mono<Void> crawlSwimcapsAsync(String url, Long logId) {
        return null;
    }

    default public void crawlProducts(String url, Long logId, ItemType itemType) {
    }


}
