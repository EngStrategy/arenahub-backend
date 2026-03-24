package com.engstrategy.arenahub_api.controller;

import com.engstrategy.arenahub_api.dto.SorteadorRequestDTO;
import com.engstrategy.arenahub_api.dto.SorteadorResponseDTO;
import com.engstrategy.arenahub_api.service.impl.SorteadorServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sorteador-times")
@RequiredArgsConstructor
@Tag(name = "Sorteador", description = "Endpoints para sorteador de times")
public class SorteadorController {

    private final SorteadorServiceImpl sorteadorService;

    @PostMapping
    @Operation(summary = "Sortear times", description = "Recebe uma lista de jogadores e realiza um sorteio aleatório de times")
    public ResponseEntity<SorteadorResponseDTO> sortearTimes(@RequestBody @Valid SorteadorRequestDTO request) {
        SorteadorResponseDTO response = sorteadorService.processarSorteio(request);
        return ResponseEntity.ok(response);
    }
}
