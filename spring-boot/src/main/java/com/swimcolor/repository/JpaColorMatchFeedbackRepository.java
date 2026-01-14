package com.swimcolor.repository;

import com.swimcolor.domain.ColorMatchFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaColorMatchFeedbackRepository extends JpaRepository<ColorMatchFeedback, Long> {
}
