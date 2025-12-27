package com.swimcolor.domain;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;

public class SwimcapIdGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) {
        String query = "SELECT nextval('swimcap_seq')";
        Long sequenceValue = ((Number) session.createNativeQuery(query, Long.class)
                .getSingleResult()).longValue();
        return String.format("SC-%04d", sequenceValue);
    }
}
