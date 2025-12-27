package com.swimcolor.controller.web;

import com.swimcolor.dto.SwimsuitListDto;
import com.swimcolor.service.SwimsuitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SwimsuitController {
    private final SwimsuitService swimsuitService;

    @GetMapping("/swimsuits")
    public String getSwimsuitList(Model model){
        List<SwimsuitListDto> allSwimsuit = swimsuitService.getAllSwimsuit();
        model.addAttribute("products",allSwimsuit);
        return "swimsuitList";
    }

    @GetMapping("/swimsuits/{id}")
    public String getSwimsuitSimilarity(@PathVariable("id")String id, Model model){
        SwimsuitListDto swimsuit = swimsuitService.getSwimsuit(id);
        model.addAttribute("swimsuit",swimsuit);
        return "swimsuit";
    }
}
