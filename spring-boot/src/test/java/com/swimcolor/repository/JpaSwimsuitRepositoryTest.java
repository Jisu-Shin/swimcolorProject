package com.swimcolor.repository;

import com.swimcolor.domain.Swimsuit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class JpaSwimsuitRepositoryTest {

    @Autowired
    JpaSwimsuitRepository swimsuitRepository;

    @Test
    public void 조회확인() throws Exception {
        //given
        Swimsuit swimsuit = Swimsuit.builder().brand("abc")
                .price(10000)
                .name("수영복")
                .productUrl("http://~")
                .imageUrl("http://~")
                .build();
        swimsuitRepository.save(swimsuit);

        //when
        Swimsuit findSuit = swimsuitRepository.findAll().get(0);
        System.out.println(findSuit);

        //then
        assertThat(findSuit.getBrand()).isEqualTo(swimsuit.getBrand());
    }

}