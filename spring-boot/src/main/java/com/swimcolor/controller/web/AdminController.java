package com.swimcolor.controller.web;

import com.swimcolor.dto.ColorMatchDto;
import com.swimcolor.dto.CrawlingLogResponseDto;
import com.swimcolor.service.ColorMatchService;
import com.swimcolor.service.CrawlingLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final ColorMatchService colorMatchService;
    private final CrawlingLogService crawlingLogService;

    @GetMapping()
    public String admin() {
        return "admin-dashboard";
    }

    @GetMapping("/logs")
    public String getCrawlingLog(Model model) {
        List<CrawlingLogResponseDto> logs = crawlingLogService.findAllCrawlingLog();

        model.addAttribute("logs", logs);
        return "admin-crawling-logs";
    }

    @GetMapping("/colormatches")
    public String getColorMatchList(Model model, @PageableDefault(size = 20) Pageable pageable) {
        Page<ColorMatchDto> page = colorMatchService.getColorMatchList(pageable);

        int nowPage = page.getNumber(); // 0부터 시작
        int startPage = Math.max(0, nowPage - 2);
        int endPage = Math.min(page.getTotalPages() - 1, nowPage + 2);

        model.addAttribute("colorMatchList", page.getContent());
        model.addAttribute("currentPage", nowPage);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "admin-color-matches";
    }
}
