package com.engstrategy.alugai_api.dto.aula;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstrutorExternoDTO {
    @NotBlank(message = "O nome do instrutor externo é obrigatório.")
    private String nome;

    @NotBlank(message = "O telefone do instrutor externo é obrigatório.")
    private String telefone;
}