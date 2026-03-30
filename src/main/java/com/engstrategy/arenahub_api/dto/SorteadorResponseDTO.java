package com.engstrategy.arenahub_api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SorteadorResponseDTO {
    private List<List<TeamPlayerDTO>> teams;
}
