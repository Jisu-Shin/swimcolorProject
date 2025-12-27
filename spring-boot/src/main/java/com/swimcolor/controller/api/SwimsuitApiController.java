package com.swimcolor.controller.api;

import com.swimcolor.dto.SwimcapListDto;
import com.swimcolor.dto.SwimsuitListDto;
import com.swimcolor.service.SwimsuitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/swimsuits")
public class SwimsuitApiController {

    private final SwimsuitService swimsuitService;

    @GetMapping("/{id}")
    public SwimsuitListDto getSwimsuit(@PathVariable String id) {
        return swimsuitService.getSwimsuit(id);
    }

    @GetMapping("/{id}/recommended-swimcaps")
    public ResponseEntity<List<SwimcapListDto>> getRecommendedSwimcaps(@PathVariable String id) {
        return ResponseEntity.ok(swimsuitService.recommendSwimcapsBySwimsuitSimilarity(id));
    }
}
