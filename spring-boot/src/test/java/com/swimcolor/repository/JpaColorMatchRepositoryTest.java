package com.swimcolor.repository;

import com.swimcolor.domain.ColorMatch;
import com.swimcolor.domain.Swimcap;
import com.swimcolor.dto.RecommendListDto;
import com.swimcolor.dto.SwimcapListDto;
import com.swimcolor.mapper.ColorMatchMapper;
import com.swimcolor.mapper.SwimcapMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaColorMatchRepositoryTest {

    @Autowired
    JpaColorMatchRepository jpaColorMatchRepository;

    private final ColorMatchMapper colorMatchMapper = Mappers.getMapper(ColorMatchMapper.class);

    @Test
    public void mapper확인() throws Exception {
        //given
        RecommendListDto dto = new RecommendListDto();
        dto.setCapHexColor("#bf6b8b");
        dto.setSuitHexColor("#be76a1");
        dto.setSimilarityScore(7.0983);
        dto.setSwimsuitId("SS-0001");
        dto.setSwimcapId("SC-0057");
        System.out.println("dto = " + dto);

        //when
        ColorMatch entity = colorMatchMapper.toEntity(dto);
        System.out.println("entity = " + entity.toString());
        jpaColorMatchRepository.save(entity);

        //then
//        assertThat(dto.getId()).isNotEmpty();
//        assertThat(dto.getColors()).isNotEmpty();
    }

}