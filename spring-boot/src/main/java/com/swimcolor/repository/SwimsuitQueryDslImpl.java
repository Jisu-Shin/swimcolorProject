package com.swimcolor.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swimcolor.domain.Swimsuit;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.swimcolor.domain.QSwimsuit.swimsuit;

@Repository
public class SwimsuitQueryDslImpl implements SwimsuitQueryDsl {
    private static final int DEFAULT_MAX_RESULTS = 50;
    private final JPAQueryFactory jpaQueryFactory;

    public SwimsuitQueryDslImpl(EntityManager entityManager) {
        this.jpaQueryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public List<Swimsuit> findBySearch(String keywords) {
        BooleanBuilder builder = new BooleanBuilder();
        for (String kw : parseKeywords(keywords)) {
            builder.and(swimsuit.name.contains(kw).or(swimsuit.brand.contains(kw)));
        }

        return jpaQueryFactory
                .selectFrom(swimsuit)
                .where(builder)
                .limit(DEFAULT_MAX_RESULTS)
                .fetch();
    }

    @Override
    public List<String> findRelatedBrands(String keywords) {
        BooleanBuilder builder = new BooleanBuilder();
        for (String kw : parseKeywords(keywords)) {
            builder.or(swimsuit.brand.contains(kw));
        }

        return jpaQueryFactory
                .select(swimsuit.brand).distinct()
                .from(swimsuit)
                .where(builder)
                .limit(DEFAULT_MAX_RESULTS)
                .fetch();
    }

    @Override
    public Page<Swimsuit> findSwimsuitsBySearchCondition(SwimsuitSearchCondition condition, Pageable pageable) {
        // 1. 데이터 조회를 위한 콘텐츠 쿼리
        List<Swimsuit> content = jpaQueryFactory
                .selectFrom(swimsuit)
                .where(
                        brandEq(condition.getBrand()) // BooleanBuilder 대신 메서드 방식 추천
                )
                .offset(pageable.getOffset())      // 시작 지점
                .orderBy(swimsuit.id.desc())
                .limit(pageable.getPageSize())     // 페이지당 개수
                .fetch();

        // 2. 전체 개수를 구하는 카운트 쿼리 (페이징의 필수 요소)
        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(swimsuit.count())
                .from(swimsuit)
                .where(
                        brandEq(condition.getBrand())
                );

        // 3. Page 객체로 변환하여 반환
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    // 💡 실무 팁: BooleanExpression을 사용하면 재사용성이 높아지고 가독성이 좋아집니다.
    private BooleanExpression brandEq(String brand) {
        return brand != null && !brand.isEmpty() ? swimsuit.brand.eq(brand) : null;
    }

    /**
     * 키워드 문자열 파싱
     * - 공백으로 분리
     * - 빈 문자열 제거
     * - 앞뒤 공백 제거
     * - 중복 제거
     *
     * @param keywords 원본 키워드 문자열
     * @return 파싱된 키워드 리스트
     */
    private List<String> parseKeywords(String keywords) {
        return Arrays.stream(keywords.split("\\s+")) // ✅ 정규식으로 공백 처리
                .map(String::trim)                    // ✅ 앞뒤 공백 제거
                .filter(StringUtils::hasText)         // ✅ 빈 문자열 제거
                .distinct()                           // ✅ 중복 제거
                .collect(Collectors.toList());
    }
}
