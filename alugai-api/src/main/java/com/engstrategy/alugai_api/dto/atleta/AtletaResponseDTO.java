package com.engstrategy.alugai_api.dto.atleta;

import com.engstrategy.alugai_api.model.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Resposta com dados do atleta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtletaResponseDTO {

    @Schema(description = "ID do atleta", example = "1")
    private Long id;

    @Schema(description = "Nome do atleta", example = "João Silva")
    private String nome;

    @Schema(description = "Email do atleta", example = "joao@email.com")
    private String email;

    @Schema(description = "Telefone do atleta", example = "(11) 99999-9999")
    private String telefone;

    @Schema(description = "URL da foto do atleta", example = "https://exemplo.com/foto.jpg")
    private String urlFoto;

    @Schema(description = "Data de criação", example = "2024-01-01T10:00:00")
    private LocalDateTime dataCriacao;

    @Schema(description = "Role do usuario", example = "ATLETA")
    @NotNull(message = "Role é obrigatória")
    private Role role;
}
