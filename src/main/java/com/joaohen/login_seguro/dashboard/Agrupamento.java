package com.joaohen.login_seguro.dashboard;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;

public enum Agrupamento {

    DIA("Por dia", "%Y-%m-%d", 93),
    SEMANA("Por semana", "%G-W%V", 371),
    MES("Por mês", "%Y-%m", 1100);

    private static final DateTimeFormatter DIA_MES = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter MES_ANO = DateTimeFormatter.ofPattern("MM/yyyy");

    private final String descricao;
    private final String formatoMongo;
    private final int limiteDias;

    Agrupamento(String descricao, String formatoMongo, int limiteDias) {
        this.descricao = descricao;
        this.formatoMongo = formatoMongo;
        this.limiteDias = limiteDias;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getFormatoMongo() {
        return formatoMongo;
    }

    public int getLimiteDias() {
        return limiteDias;
    }

    public LocalDate inicio(LocalDate data) {
        return switch (this) {
            case DIA -> data;
            case SEMANA -> data.with(DayOfWeek.MONDAY);
            case MES -> data.withDayOfMonth(1);
        };
    }

    public LocalDate proximo(LocalDate data) {
        return switch (this) {
            case DIA -> data.plusDays(1);
            case SEMANA -> data.plusWeeks(1);
            case MES -> data.plusMonths(1);
        };
    }

    public String chave(LocalDate data) {
        return switch (this) {
            case DIA -> data.toString();
            case SEMANA -> String.format("%d-W%02d",
                    data.get(IsoFields.WEEK_BASED_YEAR), data.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
            case MES -> String.format("%d-%02d", data.getYear(), data.getMonthValue());
        };
    }

    public String rotulo(LocalDate data) {
        return switch (this) {
            case DIA -> DIA_MES.format(data);
            case SEMANA -> "Semana de " + DIA_MES.format(data);
            case MES -> MES_ANO.format(data);
        };
    }
}