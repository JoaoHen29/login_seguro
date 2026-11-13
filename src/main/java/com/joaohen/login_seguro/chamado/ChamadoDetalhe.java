package com.joaohen.login_seguro.chamado;

import java.time.Instant;
import java.util.List;

public record ChamadoDetalhe(ChamadoResumo chamado, String descricao, int prazoHoras,
                             Instant inicioAtendimentoEm, Instant atualizadoEm,
                             List<Acompanhamento> historico) {

    public static ChamadoDetalhe de(Chamado chamado) {
        return new ChamadoDetalhe(ChamadoResumo.de(chamado), chamado.getDescricao(),
                chamado.getPrioridade().getPrazoHoras(), chamado.getInicioAtendimentoEm(),
                chamado.getAtualizadoEm(), List.copyOf(chamado.getHistorico()));
    }
}