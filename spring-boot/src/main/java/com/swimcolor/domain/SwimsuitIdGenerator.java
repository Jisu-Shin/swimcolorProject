package com.swimcolor.domain;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.EventTypeSets;

import java.io.Serializable;
import java.util.EnumSet;

public class SwimsuitIdGenerator implements BeforeExecutionGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {
        String query = "SELECT nextval('swimsuit_seq')";
        Long sequenceValue = ((Number) session.createNativeQuery(query, Long.class)
                .getSingleResult()).longValue();
        //todo 시퀀스 순환인지 아닌지 초과될경우는 어떻게 되는지 한번 고려해봐야함
        return String.format("SS-%04d", sequenceValue);
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EventTypeSets.INSERT_ONLY;
    }
}
