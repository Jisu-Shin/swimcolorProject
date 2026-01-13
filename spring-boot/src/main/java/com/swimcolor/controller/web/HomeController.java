package com.swimcolor.controller.web;

import com.swimcolor.dto.SwimsuitListDto;
import com.swimcolor.service.SwimsuitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final SwimsuitService swimsuitService;

    @GetMapping("/")
    public String home(Model model) {
        List<SwimsuitListDto> list = swimsuitService.getPopularSwimsuit();
        log.debug("@@@@@ 인기있는 수영복 조회 개수:{}", list.size());

        model.addAttribute("products", list);
        return "home";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }
}
