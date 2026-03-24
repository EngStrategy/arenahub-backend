package com.engstrategy.arenahub_api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SorteadorRequestDTO {
    @NotBlank
    private String lista;

    @Min(2)
    private Integer quantidadeTimes;
}