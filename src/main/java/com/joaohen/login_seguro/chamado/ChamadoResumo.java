package com.joaohen.login_seguro.chamado;

import java.time.Instant;

public record ChamadoResumo(String id, String protocolo, String titulo,
                            TipoChamado tipo, String tipoDescricao,
                            Prioridade prioridade, String prioridadeDescricao,
                            StatusChamado status, String statusDescricao,
                            String solicitante, String setor, String tecnicoResponsavel,
                            Instant abertoEm, Instant encerradoEm, boolean prazoEstourado) {

    public static ChamadoResumo de(Chamado chamado) {
        return new ChamadoResumo(
                chamado.getId(), chamado.getProtocolo(), chamado.getTitulo(),
                chamado.getTipo(), chamado.getTipo().getDescricao(),
                chamado.getPrioridade(), chamado.getPrioridade().getDescricao(),
                chamado.getStatus(), chamado.getStatus().getDescricao(),
                chamado.getSolicitante(), chamado.getSetor(), chamado.getTecnicoResponsavel(),
                chamado.getAbertoEm(), chamado.getEncerradoEm(), chamado.isPrazoEstourado());
    }
}