package com.engstrategy.alugai_api.dto.aula;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstrutorDetalhesDTO {
    private UUID id;
    private String nome;
    private String telefone;
    private String urlFoto;
}