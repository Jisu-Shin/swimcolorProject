package com.swimcolor.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.swimcolor.domain.Swimsuit;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.swimcolor.domain.QSwimsuit.swimsuit;

@Repository
public class SwimsuitQueryDslImpl implements SwimsuitQueryDsl {
    private final JPAQueryFactory jpaQueryFactory;
    public SwimsuitQueryDslImpl(EntityManager entityManager) {
        this.jpaQueryFactory = new JPAQueryFactory(entityManager);
    }

    private static final int DEFAULT_MAX_RESULTS = 50;

    @Override
    public List<Swimsuit> findBySearch(String keywords) {
        BooleanBuilder builder = new BooleanBuilder();
        for(String kw : parseKeywords(keywords)) {
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
        for(String kw : parseKeywords(keywords)) {
            builder.or(swimsuit.brand.contains(kw));
        }

        return jpaQueryFactory
                .select(swimsuit.brand).distinct()
                .from(swimsuit)
                .where(builder)
                .limit(DEFAULT_MAX_RESULTS)
                .fetch();
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
