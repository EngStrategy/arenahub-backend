package com.engstrategy.alugai_api.model.enums;

public enum DuracaoReserva {
    TRINTA_MINUTOS(30),
    UMA_HORA(60),
    UMA_HORA_E_MEIA(90),
    DUAS_HORAS(120);

    private final int minutos;

    DuracaoReserva(int minutos) {
        this.minutos = minutos;
    }

    public int getMinutos() {
        return minutos;
    }
}
