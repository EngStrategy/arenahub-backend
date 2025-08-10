package com.engstrategy.alugai_api.model.enums;

public enum StatusAgendamento {
    PENDENTE,
    AUSENTE,
    CANCELADO,
    PAGO,
    FINALIZADO; // status virtual para representar os status: AUSENTE, CANCELADO e PAGO
}
