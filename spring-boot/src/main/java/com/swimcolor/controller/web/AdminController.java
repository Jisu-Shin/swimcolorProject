package com.swimcolor.controller.web;

import com.swimcolor.dto.CrawlingLogResponseDto;
import com.swimcolor.service.AdminService;
import com.swimcolor.service.CrawlingLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final CrawlingLogService crawlingLogService;

    @GetMapping()
    public String admin() {
        return "admin";
    }

    @GetMapping("/logs")
    public String getCrawlingLog(Model model) {
        List<CrawlingLogResponseDto> logs = crawlingLogService.findAllCrawlingLog();

        model.addAttribute("logs", logs);
        return "crawlingLog";
    }
}
