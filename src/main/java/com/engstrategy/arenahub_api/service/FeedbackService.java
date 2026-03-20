package com.engstrategy.arenahub_api.service;

import com.engstrategy.arenahub_api.dto.feedback.FeedbackCreateDTO;

public interface FeedbackService {
    void salvarFeedback(FeedbackCreateDTO feedbackCreateDTO);
}