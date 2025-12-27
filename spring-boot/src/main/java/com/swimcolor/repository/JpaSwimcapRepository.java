package com.swimcolor.repository;

import com.swimcolor.domain.Swimcap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaSwimcapRepository extends JpaRepository<Swimcap, String> {
    List<Swimcap> findByIdIn(List<String> ids);
}
