package com.engstrategy.alugai_api.dto.zenvia;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class ZenviaSmsRequest {
    private String from;
    private String to;
    private List<ContentDTO> contents;
}