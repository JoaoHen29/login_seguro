package com.joaohen.login_seguro.chamado;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public record FiltroChamados(LocalDate inicio, LocalDate fim, StatusChamado status,
                             TipoChamado tipo, Prioridade prioridade, String tecnico) {

    public static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    public FiltroChamados {
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new FiltroInvalidoException("A data inicial não pode ser posterior à data final.");
        }
        if (tecnico != null && tecnico.isBlank()) {
            tecnico = null;
        }
    }

    public FiltroChamados comPeriodo(LocalDate novoInicio, LocalDate novoFim) {
        return new FiltroChamados(novoInicio, novoFim, status, tipo, prioridade, tecnico);
    }

    public Instant inicioDoPeriodo() {
        return inicio == null ? null : inicio.atStartOfDay(FUSO).toInstant();
    }

    public Instant fimDoPeriodo() {
        return fim == null ? null : fim.plusDays(1).atStartOfDay(FUSO).toInstant();
    }
}