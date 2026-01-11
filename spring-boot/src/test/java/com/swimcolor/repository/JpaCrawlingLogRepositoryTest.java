package com.swimcolor.repository;

import com.swimcolor.domain.CrawlingLog;
import com.swimcolor.domain.ItemType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaCrawlingLogRepositoryTest {

    @Autowired
    JpaCrawlingLogRepository crawlingLogRepository;

    @Test
    public void 최근동작중로그확인() throws Exception {
        //given
        CrawlingLog lastLogByItemType = crawlingLogRepository.findLastLogByItemType(ItemType.SWIMCAP)
                .stream().findFirst().orElse(null);
        System.out.println("lastLogByItemType = " + lastLogByItemType);

        CrawlingLog swimsuitLastLog = crawlingLogRepository.findLastLogByItemType(ItemType.SWIMSUIT)
                .stream().findFirst().orElse(null);
        System.out.println("swimsuit Last log = " + swimsuitLastLog);
    }

}