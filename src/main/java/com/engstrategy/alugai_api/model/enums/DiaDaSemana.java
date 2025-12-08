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

    /**
     * Converte o Enum customizado para o java.time.DayOfWeek
     */
    public java.time.DayOfWeek toJavaDayOfWeek() {
        return switch (this) {
            case SEGUNDA -> java.time.DayOfWeek.MONDAY;
            case TERCA -> java.time.DayOfWeek.TUESDAY;
            case QUARTA -> java.time.DayOfWeek.WEDNESDAY;
            case QUINTA -> java.time.DayOfWeek.THURSDAY;
            case SEXTA -> java.time.DayOfWeek.FRIDAY;
            case SABADO -> java.time.DayOfWeek.SATURDAY;
            case DOMINGO -> java.time.DayOfWeek.SUNDAY;
        };
    }

    /**
     * Deixa o ENUM legível pro Usuário
     */
    public String toReadableString() {
        return switch (this) {
            case SEGUNDA -> "Segunda-feira";
            case TERCA -> "Terça-feira";
            case QUARTA -> "Quarta-feira";
            case QUINTA -> "Quinta-feira";
            case SEXTA -> "Sexta-feira";
            case SABADO -> "Sábado";
            case DOMINGO -> "Domingo";
        };
    }
}
