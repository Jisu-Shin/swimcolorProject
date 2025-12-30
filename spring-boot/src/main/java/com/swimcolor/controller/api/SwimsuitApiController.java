package com.swimcolor.controller.api;

import com.swimcolor.dto.RecommendRequestDto;
import com.swimcolor.dto.SwimcapListDto;
import com.swimcolor.dto.SwimsuitListDto;
import com.swimcolor.service.RecommendationService;
import com.swimcolor.service.SwimsuitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/swimsuits")
public class SwimsuitApiController {

    private final SwimsuitService swimsuitService;
    private final RecommendationService recommendationService;

    @GetMapping("/{id}")
    public SwimsuitListDto getSwimsuit(@PathVariable String id) {
        return swimsuitService.getSwimsuit(id);
    }

    @PostMapping("/{id}/recommended-swimcaps")
    public ResponseEntity<List<SwimcapListDto>> getRecommendSwimcaps(@PathVariable String id, @RequestBody RecommendRequestDto requestDto) {
        return ResponseEntity.ok(recommendationService.recommendSwimcaps(id, requestDto.getColors()));
    }
}
