package com.engstrategy.alugai_api.model.enums;

public enum TipoEsporte {
    FUTEBOL_SOCIETY("Futebol Society"),
    FUTEBOL_SETE("Futebol Sete"),
    FUTEBOL_ONZE("Futebol Onze"),
    FUTSAL("Futsal"),
    FUTEBOL_AREIA("Futebol de areia"),
    BEACHTENNIS("Beach Tennis"),
    VOLEI("Vôlei"),
    FUTEVOLEI("Futevôlei"),
    BASQUETE("Basquete"),
    HANDEBOL("Handebol");


    private final String apelido;

    TipoEsporte(String apelido) {
        this.apelido = apelido;
    }

    public String getApelido() {
        return apelido;
    }
}
