package com.swimcolor.domain;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.EventTypeSets;

import java.io.Serializable;
import java.util.EnumSet;

@Slf4j
public class SwimsuitIdGenerator implements BeforeExecutionGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {
        String dialectName = session.getJdbcServices()
                .getDialect()
                .getClass()
                .getSimpleName();

        log.debug("\n@@@@@@ dialectName 확인하기 : {} \n", dialectName);

        Long sequenceValue;

        if(dialectName.contains("MySQL")) {
            // MySQL - LAST_INSERT_ID()로 동시성 문제 해결 (세션별 독립적)
            String updateQuery = "UPDATE sequences SET currval = LAST_INSERT_ID(currval + 1) WHERE name = 'swimsuit-id-seq'";
            session.createNativeMutationQuery(updateQuery).executeUpdate();

            String selectQuery = "SELECT LAST_INSERT_ID()";
            sequenceValue = ((Number) session.createNativeQuery(selectQuery, Long.class)
                    .getSingleResult()).longValue();

        } else {
            // H2, PostgreSQL
            String query = "SELECT nextval('swimsuit_seq')";
            sequenceValue = ((Number) session.createNativeQuery(query, Long.class)
                    .getSingleResult()).longValue();
        }

        return String.format("SS-%04d", sequenceValue);
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EventTypeSets.INSERT_ONLY;
    }
}
