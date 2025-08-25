package com.engstrategy.alugai_api.service.impl;

import com.engstrategy.alugai_api.dto.feedback.FeedbackCreateDTO;
import com.engstrategy.alugai_api.model.Feedback;
import com.engstrategy.alugai_api.repository.FeedbackRepository;
import com.engstrategy.alugai_api.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
     private final EmailService emailService;

    @Override
    public void salvarFeedback(FeedbackCreateDTO feedbackCreateDTO) {
        Feedback novoFeedback = Feedback.builder()
                .nome(feedbackCreateDTO.getNome())
                .email(feedbackCreateDTO.getEmail())
                .tipo(feedbackCreateDTO.getTipo())
                .mensagem(feedbackCreateDTO.getMensagem())
                .build();

        Feedback feedbackSalvo = feedbackRepository.save(novoFeedback);

         emailService.notificarAdminNovoFeedback(feedbackSalvo);
    }
}