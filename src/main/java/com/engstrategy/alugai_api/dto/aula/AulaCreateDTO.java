package com.engstrategy.alugai_api.dto.aula;

import com.engstrategy.alugai_api.model.enums.TipoEsporte;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@Jacksonized
public record AulaCreateDTO (
        @NotNull(message = "O ID da quadra é obrigatório.") Long quadraId,

        @NotNull(message = "O esporte é obrigatório.") TipoEsporte esporte,

        @NotBlank(message = "O nome da aula é obrigatório.") String nomeAula,

        @NotNull(message = "A data de início prevista é obrigatória.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate dataInicioPrevista,

        @Valid @NotEmpty(message = "A aula deve ter pelo menos um detalhe de dia e horário definido.")
        List<DetalheDiaAulaDTO> detalhesDias,

        @NotNull(message = "O limite de atletas (vagas) é obrigatório.")
        @Min(value = 1, message = "O limite de atletas deve ser maior que zero.")
        Integer limiteAtletas,

        // Instrutor (pode ser existente ou novo externo)
        UUID instrutorExistenteId,

        InstrutorExternoDTO novoInstrutorExterno,

        @NotNull(message = "O valor base mensal (por recorrência) é obrigatório.")
        @Min(value = 0, message = "O valor base deve ser positivo.")
        BigDecimal valorBaseMensal, // Valor por 1 mês de recorrência

        @NotNull(message = "O valor do plano trimestral é obrigatório.") BigDecimal valorPlanoTrimestral,

        @NotNull(message = "O valor do plano semestral é obrigatório.") BigDecimal valorPlanoSemestral,

        boolean isElegivelWellhub
) {}