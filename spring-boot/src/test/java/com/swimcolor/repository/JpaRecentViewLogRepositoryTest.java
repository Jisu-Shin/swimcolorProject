package com.swimcolor.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaRecentViewLogRepositoryTest {

    @Autowired
    JpaRecentViewLogRepository jpaRecentViewLogRepository;

    @Test
    public void 날짜비교하기() throws Exception {
        //given
        jpaRecentViewLogRepository.getDateDiff("5");

        //when

        //then
    }

}