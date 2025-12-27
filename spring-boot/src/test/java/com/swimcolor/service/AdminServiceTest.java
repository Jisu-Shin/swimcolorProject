package com.swimcolor.service;

import com.swimcolor.domain.Swimsuit;
import com.swimcolor.repository.JpaSwimsuitRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AdminServiceTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private JpaSwimsuitRepository swimsuitRepository;

    @Test
    void 관리자가_크롤링_서비스_호출시_DB저장까지_정상_동작한다() {
        // given
        String testUrl = "https://swim.co.kr/categories/918698/products?childCategoryNo=919173&brands=%255B43160579%255D&pageNumber=1&categoryNos=%255B%255D";

        // when
        adminService.crawl(testUrl);

        // then
        List<Swimsuit> all = swimsuitRepository.findAll();
        assertThat(all).isNotEmpty(); // 실제로 DB에 크롤링 결과가 저장돼야 성공
        System.out.println("DB 저장 개수: " + all.size());
    }
}
