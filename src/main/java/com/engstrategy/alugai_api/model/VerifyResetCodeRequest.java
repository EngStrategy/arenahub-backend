package com.engstrategy.alugai_api.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyResetCodeRequest {
    @NotBlank
    private String code;

    @NotBlank
    @Email
    private String email;
}
