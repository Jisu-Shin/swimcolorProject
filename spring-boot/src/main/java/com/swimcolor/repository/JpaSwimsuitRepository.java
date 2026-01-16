package com.swimcolor.repository;

import com.swimcolor.domain.Swimsuit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaSwimsuitRepository extends JpaRepository<Swimsuit, String>, SwimsuitQueryDsl {
}
