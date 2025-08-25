package com.engstrategy.alugai_api.controller;

import com.engstrategy.alugai_api.dto.feedback.FeedbackCreateDTO;
import com.engstrategy.alugai_api.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/feedback")
@Tag(name = "Feedback", description = "Endpoint para envio de feedback dos usuários")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    @Operation(summary = "Receber feedback de um usuário")
    public ResponseEntity<Map<String, String>> receberFeedback(@Valid @RequestBody FeedbackCreateDTO feedbackCreateDTO) {
        feedbackService.salvarFeedback(feedbackCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Feedback recebido com sucesso. Obrigado!"));
    }
}