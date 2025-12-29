package com.swimcolor.repository;

import com.swimcolor.domain.Swimcap;
import com.swimcolor.dto.SwimcapListDto;
import com.swimcolor.mapper.SwimcapMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaSwimcapRepositoryTest {

    @Autowired
    JpaSwimcapRepository swimcapRepository;

    private final SwimcapMapper swimcapMapper = Mappers.getMapper(SwimcapMapper.class);

    @Test
    public void mapper확인() throws Exception {
        //given
        Swimcap swimcap = Swimcap.builder().brand("abc")
                .price(10000)
                .name("수모")
                .productUrl("http://~")
                .imageUrl("http://~")
                .colors(List.of("#1234","#5678"))
                .build();
        swimcapRepository.save(swimcap);
        Swimcap entity = swimcapRepository.findAll().get(0);
        System.out.println("swimcap = " + entity);

        //when
        SwimcapListDto dto = swimcapMapper.toDto(entity);
        System.out.println("dto = " + dto);

        //then
        assertThat(dto.getId()).isNotEmpty();
        assertThat(dto.getColors()).isNotEmpty();
    }
}