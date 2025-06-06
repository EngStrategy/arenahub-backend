package com.engstrategy.alugai_api.dto.arena;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Resposta com dados da arena")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArenaResponseDTO {

    @Schema(description = "ID da arena", example = "1")
    private Long id;

    @Schema(description = "Nome da arena", example = "Arena Sports Center")
    private String nome;

    @Schema(description = "Email da arena", example = "contato@arena.com")
    private String email;

    @Schema(description = "Telefone da arena", example = "(11) 99999-9999")
    private String telefone;

    @Schema(description = "CPF do proprietário", example = "123.456.789-00")
    private String cpfProprietario;

    @Schema(description = "CNPJ da arena", example = "12.345.678/0001-90")
    private String cnpj;

    @Schema(description = "Endereço da arena")
    private EnderecoDTO endereco;

    @Schema(description = "Descrição da arena")
    private String descricao;

    @Schema(description = "URL da foto da arena")
    private String urlFoto;

    @Schema(description = "Data de criação", example = "2024-01-01T10:00:00")
    private LocalDateTime dataCriacao;
}
