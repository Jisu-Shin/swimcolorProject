package com.swimcolor.repository;

import com.swimcolor.domain.Swimcap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaSwimcapRepository extends JpaRepository<Swimcap, String> {
    @Query("SELECT s FROM Swimcap s LEFT JOIN FETCH s.colors WHERE s.id IN :ids")
    List<Swimcap> findByIdsWithColors(@Param("ids") List<String> ids);
}
