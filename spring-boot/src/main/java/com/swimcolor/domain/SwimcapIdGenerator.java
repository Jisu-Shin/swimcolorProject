package com.swimcolor.domain;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.EventTypeSets;

import java.io.Serializable;
import java.util.EnumSet;

@Slf4j
public class SwimcapIdGenerator implements BeforeExecutionGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {
        String dialectName = session.getJdbcServices()
                .getDialect()
                .getClass()
                .getSimpleName();

        log.info("\n@@@@@@ dialectName 확인하기 : {} \n", dialectName);

        Long sequenceValue;

        if(dialectName.contains("MySQL")) {
            // MySQL - LAST_INSERT_ID()로 동시성 문제 해결 (세션별 독립적)
            String updateQuery = "UPDATE sequences SET currval = LAST_INSERT_ID(currval + 1) WHERE name = 'swimcap-id-seq'";
            session.createNativeMutationQuery(updateQuery).executeUpdate();

            String selectQuery = "SELECT LAST_INSERT_ID()";
            sequenceValue = ((Number) session.createNativeQuery(selectQuery, Long.class)
                    .getSingleResult()).longValue();

        } else {
            // H2, PostgreSQL
            String query = "SELECT nextval('swimcap_seq')";
            sequenceValue = ((Number) session.createNativeQuery(query, Long.class)
                    .getSingleResult()).longValue();
        }

        //todo 시퀀스 순환인지 아닌지 초과될경우는 어떻게 되는지 한번 고려해봐야함
        return String.format("SC-%04d", sequenceValue);
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EventTypeSets.INSERT_ONLY;
    }
}
