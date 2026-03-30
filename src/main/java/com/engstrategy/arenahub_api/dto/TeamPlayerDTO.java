package com.engstrategy.arenahub_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamPlayerDTO {
    private String name;
    
       private boolean isGoalkeeper;
}
