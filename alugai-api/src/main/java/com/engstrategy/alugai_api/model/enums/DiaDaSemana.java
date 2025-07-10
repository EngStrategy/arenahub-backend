package com.engstrategy.alugai_api.model.enums;

import java.time.DayOfWeek;
import java.time.LocalDate;

public enum DiaDaSemana {
    SEGUNDA,
    TERCA,
    QUARTA,
    QUINTA,
    SEXTA,
    SABADO,
    DOMINGO;

    /**
     * Converte um LocalDate para DiaDaSemana
     */
    public static DiaDaSemana fromLocalDate(LocalDate data) {
        DayOfWeek dayOfWeek = data.getDayOfWeek();
        return switch (dayOfWeek) {
            case MONDAY -> SEGUNDA;
            case TUESDAY -> TERCA;
            case WEDNESDAY -> QUARTA;
            case THURSDAY -> QUINTA;
            case FRIDAY -> SEXTA;
            case SATURDAY -> SABADO;
            case SUNDAY -> DOMINGO;
        };
    }
}
