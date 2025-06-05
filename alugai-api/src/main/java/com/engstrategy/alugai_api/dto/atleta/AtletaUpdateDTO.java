package com.engstrategy.alugai_api.dto.atleta;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Schema(description = "Dados para atualização de um atleta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtletaUpdateDTO {

    @Schema(description = "Nome do atleta", example = "João Silva")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nome;

    @Schema(description = "Telefone do atleta", example = "(11) 99999-9999")
    @Pattern(regexp = "\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}", message = "Telefone deve estar no formato (XX) XXXXX-XXXX")
    private String telefone;

    @Schema(description = "URL da foto do atleta", example = "https://exemplo.com/foto.jpg")
    @URL(message = "URL da foto deve ser válida")
    private String urlFoto;
}
