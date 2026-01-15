package com.swimcolor.repository;

import com.swimcolor.domain.ColorMatchFeedback;
import com.swimcolor.domain.FeedbackType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaColorMatchFeedbackRepository extends JpaRepository<ColorMatchFeedback, Long> {
    boolean existsByColorMatchIdAndFeedbackType(Long colorMatchId, FeedbackType feedbackType);
}
