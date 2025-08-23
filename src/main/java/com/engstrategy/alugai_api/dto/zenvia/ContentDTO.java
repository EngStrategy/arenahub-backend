package com.engstrategy.alugai_api.dto.zenvia;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ContentDTO {
    private String type = "text";
    private String text;
}