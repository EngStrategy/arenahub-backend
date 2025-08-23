package com.engstrategy.alugai_api.dto.arena;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Schema(description = "Dados para atualização de uma arena")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArenaUpdateDTO {

    @Schema(description = "Nome da arena", example = "Arena Sports Center")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nome;

    @Schema(description = "Telefone da arena", example = "(11) 99999-9999")
    @Pattern(regexp = "\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}", message = "Telefone deve estar no formato (XX) XXXXX-XXXX")
    private String telefone;

    @Schema(description = "Endereço da arena")
    @Valid
    private EnderecoDTO endereco;

    @Schema(description = "Até quantas horas antes o atleta pode cancelar", required = true)
    @NotNull(message = "Hora de antecedencia de cancelamento é obrigatório")
    @Min(value = 0, message = "O valor não pode ser negativo.")
    private Integer horasCancelarAgendamento;

    @Schema(description = "Descrição da arena", example = "Arena com quadras de futebol society e tênis")
    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String descricao;

    @Schema(description = "URL da foto da arena", example = "https://exemplo.com/foto.jpg")
    @URL(message = "URL da foto deve ser válida")
    private String urlFoto;
}
