package com.engstrategy.alugai_api.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
    private String newPassword;

    @NotBlank
    private String confirmation;

    @AssertTrue(message = "As senhas não coincidem")
    public boolean isPasswordMatch() {
        return newPassword != null && newPassword.equals(confirmation);
    }
}
