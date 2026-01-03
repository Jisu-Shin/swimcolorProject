package com.swimcolor.controller.web;

import com.swimcolor.dto.SwimsuitListDto;
import com.swimcolor.service.SwimsuitService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SwimsuitController {
    private final SwimsuitService swimsuitService;

    @GetMapping("/swimsuits")
    public String getSwimsuitList(Model model, @RequestParam(value="page", defaultValue="0") int page){
        Page<SwimsuitListDto> products = swimsuitService.getSwimsuitList(page);
        model.addAttribute("products",products);
        return "swimsuitList";
    }

    @GetMapping("/swimsuits/{id}")
    public String getSwimsuitSimilarity(@PathVariable("id")String id, Model model){
        SwimsuitListDto swimsuit = swimsuitService.getSwimsuit(id);
        model.addAttribute("swimsuit",swimsuit);
        return "swimsuit";
    }
}
