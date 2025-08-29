package com.engstrategy.alugai_api.dto.arena;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Dados de endereço")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnderecoDTO {

    @Schema(description = "CEP", example = "12345-678", required = true)
    @NotBlank(message = "CEP é obrigatório")
    @Pattern(regexp = "\\d{5}-\\d{3}", message = "CEP deve estar no formato XXXXX-XXX")
    private String cep;

    @Schema(description = "Estado", example = "SP", required = true)
    @NotBlank(message = "Estado é obrigatório")
    @Size(min = 2, max = 2, message = "Estado deve ter 2 caracteres")
    private String estado;

    @Schema(description = "Cidade", example = "São Paulo", required = true)
    @NotBlank(message = "Cidade é obrigatória")
    private String cidade;

    @Schema(description = "Bairro", example = "Centro", required = true)
    @NotBlank(message = "Bairro é obrigatório")
    private String bairro;

    @Schema(description = "Rua", example = "Rua das Flores", required = true)
    @NotBlank(message = "Rua é obrigatória")
    private String rua;

    @Schema(description = "Número", example = "123", required = true)
    @NotBlank(message = "Número é obrigatório")
    private String numero;

    @Schema(description = "Complemento", example = "Apto 101")
    private String complemento;

    @NotNull(message = "A latitude é obrigatória")
    @Min(value = -90, message = "Latitude deve ser no mínimo -90")
    @Max(value = 90, message = "Latitude deve ser no máximo 90")
    private Double latitude;

    @NotNull(message = "A longitude é obrigatória")
    @Min(value = -180, message = "Longitude deve ser no mínimo -180")
    @Max(value = 180, message = "Longitude deve ser no máximo 180")
    private Double longitude;
}
