package com.engstrategy.alugai_api.service;

import com.engstrategy.alugai_api.dto.feedback.FeedbackCreateDTO;

public interface FeedbackService {
    void salvarFeedback(FeedbackCreateDTO feedbackCreateDTO);
}