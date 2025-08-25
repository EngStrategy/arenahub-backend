package com.engstrategy.alugai_api.dto.feedback;

import com.engstrategy.alugai_api.model.enums.TipoFeedback;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackCreateDTO {

    @NotBlank(message = "O nome é obrigatório.")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres.")
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "O formato do e-mail é inválido.")
    private String email;

    @NotNull(message = "O tipo de feedback é obrigatório.")
    private TipoFeedback tipo;

    @NotBlank(message = "A mensagem é obrigatória.")
    @Size(min = 10, max = 2000, message = "A mensagem deve ter entre 10 e 2000 caracteres.")
    private String mensagem;
}