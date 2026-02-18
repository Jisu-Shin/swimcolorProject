package com.swimcolor.repository;

import com.swimcolor.domain.Swimsuit;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SwimsuitQueryDslImpl 테스트
 * - @DataJpaTest: JPA 관련 컴포넌트만 로드 (가볍고 빠름)
 * - @Import: QueryDSL 구현체 수동 추가
 * - 인메모리 H2 DB 사용
 */
@DataJpaTest
@ActiveProfiles("h2")
class SwimsuitQueryDslImplTest {

    @Autowired
    private EntityManager entityManager;
    
    @Autowired
    private SwimsuitQueryDslImpl swimsuitQueryDsl;

    @Autowired
    private JpaSwimsuitRepository swimsuitRepository;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화 (각 테스트마다 자동 롤백됨)
        swimsuitRepository.deleteAll();
        entityManager.flush(); // ✅ 삭제 즉시 반영
        entityManager.clear(); // ✅ 영속성 컨텍스트 초기화
        
        // 테스트용 수영복 데이터 생성
        Swimsuit swimsuit1 = Swimsuit.builder()
                .name("나이키 수영복 블랙")
                .brand("나이키")
                .imageUrl("http://example.com/image1.jpg")
                .productUrl("http://example.com/product1")
                .price(50000)
                .colors(List.of("#000000", "#FFFFFF"))
                .crawlingLogId(1L)
                .build();

        Swimsuit swimsuit2 = Swimsuit.builder()
                .name("아디다스 수영복 블루")
                .brand("아디다스")
                .imageUrl("http://example.com/image2.jpg")
                .productUrl("http://example.com/product2")
                .price(60000)
                .colors(List.of("#000000", "#FFFFFF"))
                .crawlingLogId(1L)
                .build();

        Swimsuit swimsuit3 = Swimsuit.builder()
                .name("스피도 수영복 레드")
                .brand("스피도")
                .imageUrl("http://example.com/image3.jpg")
                .productUrl("http://example.com/product3")
                .price(70000)
                .colors(List.of("#000000", "#FFFFFF"))
                .crawlingLogId(1L)
                .build();

        Swimsuit swimsuit4 = Swimsuit.builder()
                .name("나이키 프로 수영복")
                .brand("나이키")
                .imageUrl("http://example.com/image4.jpg")
                .productUrl("http://example.com/product4")
                .price(80000)
                .colors(List.of("#000000", "#FFFFFF"))
                .crawlingLogId(1L)
                .build();

        Swimsuit swimsuit5 = Swimsuit.builder()
                .name("아레나 수영복")
                .brand("아레나")
                .imageUrl("http://example.com/image5.jpg")
                .productUrl("http://example.com/product5")
                .price(55000)
                .colors(List.of("#000000", "#FFFFFF"))
                .crawlingLogId(1L)
                .build();

        // 색상이 없는 수영복 (조건에서 제외되어야 함)
        Swimsuit swimsuit6 = Swimsuit.builder()
                .name("푸마 수영복")
                .brand("푸마")
                .imageUrl("http://example.com/image6.jpg")
                .productUrl("http://example.com/product6")
                .price(45000)
                .colors(List.of()) // 빈 리스트
                .crawlingLogId(1L)
                .build();

        swimsuitRepository.saveAll(List.of(
                swimsuit1, swimsuit2, swimsuit3, swimsuit4, swimsuit5, swimsuit6
        ));

    }

    @Test
    @DisplayName("브랜드 조건으로 검색 - 단일 브랜드")
    void findSwimsuitsBySearchCondition_SingleBrand() {
        // given
        SwimsuitSearchCondition condition = new SwimsuitSearchCondition();
        condition.setBrands(List.of("나이키"));
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Swimsuit> result = swimsuitQueryDsl.findSwimsuitsBySearchCondition(condition, pageable);

        // then
        assertThat(result.getContent()).hasSize(2); // 나이키 2개
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .allMatch(s -> s.getBrand().contains("나이키"));
        assertThat(result.getContent())
                .allMatch(s -> !s.getColors().isEmpty()); // 색상이 있어야 함
    }

    @Test
    @DisplayName("브랜드 조건으로 검색 - 여러 브랜드 (OR 조건)")
    void findSwimsuitsBySearchCondition_MultipleBrands() {
        // given
        SwimsuitSearchCondition condition = new SwimsuitSearchCondition();
        condition.setBrands(List.of("나이키", "아디다스"));
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Swimsuit> result = swimsuitQueryDsl.findSwimsuitsBySearchCondition(condition, pageable);

        // then
        assertThat(result.getContent()).hasSize(3); // 나이키 2개 + 아디다스 1개
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent())
                .allMatch(s -> s.getBrand().contains("나이키") || s.getBrand().contains("아디다스"));
    }

    @Test
    @DisplayName("브랜드 조건으로 검색 - 빈 브랜드 리스트")
    void findSwimsuitsBySearchCondition_EmptyBrands() {
        // given
        SwimsuitSearchCondition condition = new SwimsuitSearchCondition();
        condition.setBrands(List.of());
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Swimsuit> result = swimsuitQueryDsl.findSwimsuitsBySearchCondition(condition, pageable);

        // then
        // 브랜드 조건이 없으면 색상이 있는 모든 수영복 반환
        assertThat(result.getContent()).hasSize(5); // 색상 있는 수영복 5개
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getContent())
                .allMatch(s -> !s.getColors().isEmpty());
    }

    @Test
    @DisplayName("브랜드 조건으로 검색 - null 브랜드 리스트")
    void findSwimsuitsBySearchCondition_NullBrands() {
        // given
        SwimsuitSearchCondition condition = new SwimsuitSearchCondition();
        condition.setBrands(null);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Swimsuit> result = swimsuitQueryDsl.findSwimsuitsBySearchCondition(condition, pageable);

        // then
        // 브랜드 조건이 없으면 색상이 있는 모든 수영복 반환
        assertThat(result.getContent()).hasSize(5); // 색상 있는 수영복 5개
        assertThat(result.getTotalElements()).isEqualTo(5);
    }

    @Test
    @DisplayName("브랜드 조건으로 검색 - 존재하지 않는 브랜드")
    void findSwimsuitsBySearchCondition_NonExistentBrand() {
        // given
        SwimsuitSearchCondition condition = new SwimsuitSearchCondition();
        condition.setBrands(List.of("존재하지않는브랜드"));
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Swimsuit> result = swimsuitQueryDsl.findSwimsuitsBySearchCondition(condition, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("브랜드 조건으로 검색 - 페이징 테스트 (첫 페이지)")
    void findSwimsuitsBySearchCondition_Paging_FirstPage() {
        // given
        SwimsuitSearchCondition condition = new SwimsuitSearchCondition();
        condition.setBrands(List.of("나이키", "아디다스", "스피도"));
        Pageable pageable = PageRequest.of(0, 2); // 페이지 크기 2

        // when
        Page<Swimsuit> result = swimsuitQueryDsl.findSwimsuitsBySearchCondition(condition, pageable);

        // then
        assertThat(result.getContent()).hasSize(2); // 2개만
        assertThat(result.getTotalElements()).isEqualTo(4); // 전체 4개
        assertThat(result.getTotalPages()).isEqualTo(2); // 총 2페이지
        assertThat(result.isFirst()).isTrue();
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("브랜드 조건으로 검색 - 페이징 테스트 (두 번째 페이지)")
    void findSwimsuitsBySearchCondition_Paging_SecondPage() {
        // given
        SwimsuitSearchCondition condition = new SwimsuitSearchCondition();
        condition.setBrands(List.of("나이키", "아디다스", "스피도"));
        Pageable pageable = PageRequest.of(1, 2); // 두 번째 페이지

        // when
        Page<Swimsuit> result = swimsuitQueryDsl.findSwimsuitsBySearchCondition(condition, pageable);

        // then
        assertThat(result.getContent()).hasSize(2); // 2개
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.isLast()).isTrue();
        assertThat(result.hasPrevious()).isTrue();
    }

    @Test
    @DisplayName("브랜드 조건으로 검색 - 색상이 없는 수영복 제외")
    void findSwimsuitsBySearchCondition_ExcludeEmptyColors() {
        // given
        SwimsuitSearchCondition condition = new SwimsuitSearchCondition();
        condition.setBrands(List.of("푸마")); // 색상 없는 브랜드
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Swimsuit> result = swimsuitQueryDsl.findSwimsuitsBySearchCondition(condition, pageable);

        // then
        // 푸마 브랜드는 있지만 색상이 없어서 제외되어야 함
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("브랜드 조건으로 검색 - 부분 일치 검색")
    void findSwimsuitsBySearchCondition_PartialMatch() {
        // given
        SwimsuitSearchCondition condition = new SwimsuitSearchCondition();
        condition.setBrands(List.of("나이")); // 부분 일치
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Swimsuit> result = swimsuitQueryDsl.findSwimsuitsBySearchCondition(condition, pageable);

        // then
        assertThat(result.getContent()).hasSize(2); // "나이키" 매칭
        assertThat(result.getContent())
                .allMatch(s -> s.getBrand().contains("나이"));
    }

    @Test
    @DisplayName("브랜드 조건으로 검색 - 정렬 확인 (ID 내림차순)")
    void findSwimsuitsBySearchCondition_OrderById() {
        // given
        SwimsuitSearchCondition condition = new SwimsuitSearchCondition();
        condition.setBrands(List.of("나이키"));
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Swimsuit> result = swimsuitQueryDsl.findSwimsuitsBySearchCondition(condition, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        // ID 내림차순 확인 (SS-0004, SS-0001)
        assertThat(result.getContent().get(0).getId()).isGreaterThan(result.getContent().get(1).getId());
    }

    @Test
    @DisplayName("브랜드 조건으로 검색 - 빈 문자열 브랜드 무시")
    void findSwimsuitsBySearchCondition_IgnoreBlankBrand() {
        // given
        SwimsuitSearchCondition condition = new SwimsuitSearchCondition();
        condition.setBrands(List.of("", "  ", "나이키")); // 빈 문자열, 공백 포함
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Swimsuit> result = swimsuitQueryDsl.findSwimsuitsBySearchCondition(condition, pageable);

        // then
        // 빈 문자열과 공백은 무시되고 "나이키"만 검색되어야 함
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .allMatch(s -> s.getBrand().contains("나이키"));
    }

    @Test
    @DisplayName("브랜드 조건으로 검색 - null 브랜드 포함된 리스트")
    void findSwimsuitsBySearchCondition_WithNullBrand() {
        // given
        SwimsuitSearchCondition condition = new SwimsuitSearchCondition();
        condition.setBrands(List.of(null, "아디다스")); // null 포함
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Swimsuit> result = swimsuitQueryDsl.findSwimsuitsBySearchCondition(condition, pageable);

        // then
        // null은 무시되고 "아디다스"만 검색되어야 함
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBrand()).isEqualTo("아디다스");
    }

    @Test
    @DisplayName("브랜드 조건으로 검색 - 모든 브랜드가 빈 값인 경우")
    void findSwimsuitsBySearchCondition_AllBrandsBlank() {
        // given
        SwimsuitSearchCondition condition = new SwimsuitSearchCondition();
        condition.setBrands(List.of("", "  ", null)); // 모두 빈 값
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Swimsuit> result = swimsuitQueryDsl.findSwimsuitsBySearchCondition(condition, pageable);

        // then
        // 유효한 브랜드가 없으면 색상 있는 모든 수영복 반환
        assertThat(result.getContent()).hasSize(5);
        assertThat(result.getTotalElements()).isEqualTo(5);
    }
}