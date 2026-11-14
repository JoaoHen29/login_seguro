package com.joaohen.login_seguro.dashboard;

import com.joaohen.login_seguro.chamado.ChamadoResumo;
import com.joaohen.login_seguro.chamado.StatusChamado;
import com.joaohen.login_seguro.chamado.TipoChamado;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class DashboardRespostas {

    private DashboardRespostas() {
    }

    public record Indicadores(long totalChamados, long pendentes, long resolvidos, long cancelados,
                              long criticosPendentes, long foraDoPrazo, double percentualForaDoPrazo,
                              double taxaResolucao, Double tempoMedioPrimeiroAtendimentoHoras,
                              Double tempoMedioResolucaoHoras, Instant geradoEm) {
    }

    public record ContagemStatus(StatusChamado status, String descricao, long total, double percentual) {
    }

    public record SituacaoChamados(long total, List<ContagemStatus> porStatus,
                                   List<ChamadoResumo> pendentesMaisAntigos) {
    }

    public record ContagemTipo(TipoChamado tipo, String descricao, long total, long resolvidos,
                               Double tempoMedioResolucaoHoras, double percentual) {
    }

    public record PontoPeriodo(String periodo, String rotulo, long abertos, long resolvidos) {
    }

    public record SeriePeriodo(Agrupamento agrupamento, LocalDate inicio, LocalDate fim,
                               long totalAbertos, long totalResolvidos, List<PontoPeriodo> pontos) {
    }

    public record Opcao(String codigo, String descricao) {
    }

    public record OpcoesFiltro(List<Opcao> status, List<Opcao> tipos, List<Opcao> prioridades,
                               List<String> tecnicos, List<Opcao> agrupamentos, LocalDate primeiraData) {
    }
}