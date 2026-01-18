package com.swimcolor.controller.web;

import com.swimcolor.service.SwimsuitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequiredArgsConstructor
public class SearchController {
    private final SwimsuitService swimsuitService;

    @GetMapping("/search")
    public String getSwimsuitSimilarity(@RequestParam(value = "keywords", required = false) String keywords, Model model) {
        swimsuitService.findBySearch(keywords)
                .ifPresent(bySearch -> {
                    model.addAttribute("keyword", keywords);
                    model.addAttribute("brands", bySearch.getBrands());
                    model.addAttribute("swimsuits", bySearch.getSwimsuitList());
                    model.addAttribute("totalCount", bySearch.getSwimsuitList().size());
                });

        return "search";
    }
}
