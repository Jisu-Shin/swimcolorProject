package com.swimcolor.repository;

import com.swimcolor.domain.Swimcap;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaSwimcapRepository extends JpaRepository<Swimcap, String> {
}
