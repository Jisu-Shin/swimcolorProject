package com.swimcolor.service;

import com.swimcolor.domain.Swimcap;
import com.swimcolor.domain.Swimsuit;
import com.swimcolor.repository.JpaSwimcapRepository;
import com.swimcolor.repository.JpaSwimsuitRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
class AdminServiceIntegrationTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private JpaSwimsuitRepository swimsuitRepository;

    @Autowired
    private JpaSwimcapRepository swimcapRepository;

    @Test
    void 수영복크롤링테스트() {
        // given
        String testUrl = "https://swim.co.kr/categories/918698/products?childCategoryNo=919173&brands=%255B43160579%255D&pageNumber=1&categoryNos=%255B%255D";

        // when
        adminService.crawlSwimsuits(testUrl);

        // then
        List<Swimsuit> all = swimsuitRepository.findAll();
        assertThat(all).isNotEmpty(); // 실제로 DB에 크롤링 결과가 저장돼야 성공
        System.out.println("DB 저장 개수: " + all.size());
    }

    @Test
    public void 수모크롤링테스트() throws Exception {
        // given
        String testUrl = "https://swim.co.kr/categories/918606/products?childCategoryNo=919019&brands=%255B43160578%255D";

        // when
        adminService.crawlSwimcaps(testUrl);

        // then
        List<Swimcap> all = swimcapRepository.findAll();
        assertThat(all).isNotEmpty(); // 실제로 DB에 크롤링 결과가 저장돼야 성공
        System.out.println("DB 저장 개수: " + all.size());
    }
}
