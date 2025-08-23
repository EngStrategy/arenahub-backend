package com.engstrategy.alugai_api.dto.arena;

import com.engstrategy.alugai_api.model.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Schema(description = "Dados para criação de uma arena")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArenaCreateDTO {

    @Schema(description = "Nome da arena", example = "Arena Sports Center", required = true)
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nome;

    @Schema(description = "Email da arena", example = "contato@arena.com", required = true)
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    private String email;

    @Schema(description = "Telefone da arena", example = "(11) 99999-9999", required = true)
    @NotBlank(message = "Telefone é obrigatório")
    @Pattern(regexp = "\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}", message = "Telefone deve estar no formato (XX) XXXXX-XXXX")
    private String telefone;

    @Schema(description = "Senha da arena", required = true, minLength = 8)
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    private String senha;

    @Schema(description = "CPF do proprietário", example = "123.456.789-00", required = true)
    @NotBlank(message = "CPF do proprietário é obrigatório")
    @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "CPF deve estar no formato XXX.XXX.XXX-XX")
    private String cpfProprietario;

    @Schema(description = "CNPJ da arena", example = "12.345.678/0001-90")
    @Pattern(regexp = "\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}", message = "CNPJ deve estar no formato XX.XXX.XXX/XXXX-XX")
    private String cnpj;

    @Schema(description = "Endereço da arena", required = true)
    @NotNull(message = "Endereço é obrigatório")
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
