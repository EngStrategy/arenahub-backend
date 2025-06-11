package com.engstrategy.alugai_api.dto.atleta;

import com.engstrategy.alugai_api.model.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Schema(description = "Dados para criação de um atleta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtletaCreateDTO {

    @Schema(description = "Nome do atleta", example = "João Silva", required = true)
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nome;

    @Schema(description = "Email do atleta", example = "joao@email.com", required = true)
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    private String email;

    @Schema(description = "Telefone do atleta", example = "(11) 99999-9999", required = true)
    @NotBlank(message = "Telefone é obrigatório")
    @Pattern(regexp = "\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}", message = "Telefone deve estar no formato (XX) XXXXX-XXXX")
    private String telefone;

    @Schema(description = "Senha do atleta", required = true, minLength = 8)
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    private String senha;

    @Schema(description = "Role do usuario", example = "ATLETA")
    @NotNull(message = "Role é obrigatória")
    private Role role;
}

