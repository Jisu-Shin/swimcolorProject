package com.swimcolor.repository;

import com.swimcolor.domain.Swimsuit;
import com.swimcolor.dto.SwimsuitListDto;
import com.swimcolor.mapper.SwimsuitMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaSwimsuitRepositoryTest {

    @Autowired
    JpaSwimsuitRepository swimsuitRepository;

    private final SwimsuitMapper swimsuitMapper = Mappers.getMapper(SwimsuitMapper.class);

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

    @Test
    public void mapper확인() throws Exception {
        //given
        // todo jpatest는 h2 메모리 db를 사용하네?
        Swimsuit swimsuit = Swimsuit.builder().brand("abc")
                .price(10000)
                .name("수영복")
                .productUrl("http://~")
                .imageUrl("http://~")
                .colors(List.of("#1234","#5678"))
                .build();
        swimsuitRepository.save(swimsuit);
        Swimsuit entity = swimsuitRepository.findAll().get(0);
        System.out.println("swimsuit = " + entity);

        //when
        SwimsuitListDto dto = swimsuitMapper.toDto(entity);
        System.out.println("dto = " + dto);

        //then
        assertThat(dto.getId()).isNotEmpty();
        assertThat(dto.getColors()).isNotEmpty();
    }

}