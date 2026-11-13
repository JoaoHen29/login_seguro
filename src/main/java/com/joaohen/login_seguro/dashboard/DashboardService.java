package com.joaohen.login_seguro.dashboard;

import com.joaohen.login_seguro.chamado.Chamado;
import com.joaohen.login_seguro.chamado.ChamadoConsultas;
import com.joaohen.login_seguro.chamado.ChamadoResumo;
import com.joaohen.login_seguro.chamado.FiltroChamados;
import com.joaohen.login_seguro.chamado.FiltroInvalidoException;
import com.joaohen.login_seguro.chamado.Prioridade;
import com.joaohen.login_seguro.chamado.StatusChamado;
import com.joaohen.login_seguro.chamado.TipoChamado;
import com.joaohen.login_seguro.dashboard.DashboardRespostas.ContagemStatus;
import com.joaohen.login_seguro.dashboard.DashboardRespostas.ContagemTipo;
import com.joaohen.login_seguro.dashboard.DashboardRespostas.Indicadores;
import com.joaohen.login_seguro.dashboard.DashboardRespostas.Opcao;
import com.joaohen.login_seguro.dashboard.DashboardRespostas.OpcoesFiltro;
import com.joaohen.login_seguro.dashboard.DashboardRespostas.PontoPeriodo;
import com.joaohen.login_seguro.dashboard.DashboardRespostas.SeriePeriodo;
import com.joaohen.login_seguro.dashboard.DashboardRespostas.SituacaoChamados;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Service
public class DashboardService {

    private static final int PENDENTES_EXIBIDOS = 5;

    private final ChamadoConsultas consultas;

    public DashboardService(ChamadoConsultas consultas) {
        this.consultas = consultas;
    }

    public Indicadores indicadores(FiltroChamados filtro) {
        List<Chamado> chamados = consultas.listarParaIndicadores(filtro);
        long total = chamados.size();
        long resolvidos = contar(chamados, c -> c.getStatus() == StatusChamado.RESOLVIDO);
        long cancelados = contar(chamados, c -> c.getStatus() == StatusChamado.CANCELADO);
        long pendentes = contar(chamados, c -> !c.getStatus().isEncerrado());
        long criticos = contar(chamados, c -> !c.getStatus().isEncerrado() && c.getPrioridade() == Prioridade.CRITICA);
        long foraDoPrazo = contar(chamados, c -> c.getStatus() != StatusChamado.CANCELADO && c.isPrazoEstourado());
        long avaliados = total - cancelados;

        Double primeiroAtendimento = mediaHoras(chamados.stream()
                .filter(c -> c.getInicioAtendimentoEm() != null)
                .map(c -> Duration.between(c.getAbertoEm(), c.getInicioAtendimentoEm())));
        Double resolucao = mediaHoras(chamados.stream()
                .filter(c -> c.getStatus() == StatusChamado.RESOLVIDO && c.getEncerradoEm() != null)
                .map(c -> Duration.between(c.getAbertoEm(), c.getEncerradoEm())));

        return new Indicadores(total, pendentes, resolvidos, cancelados, criticos, foraDoPrazo,
                percentual(foraDoPrazo, avaliados), percentual(resolvidos, avaliados),
                primeiroAtendimento, resolucao, Instant.now());
    }

    public SituacaoChamados situacao(FiltroChamados filtro) {
        Map<StatusChamado, Long> totais = consultas.contarPorStatus(filtro);
        long total = totais.values().stream().mapToLong(Long::longValue).sum();
        List<ContagemStatus> porStatus = Arrays.stream(StatusChamado.values())
                .map(status -> {
                    long quantidade = totais.getOrDefault(status, 0L);
                    return new ContagemStatus(status, status.getDescricao(), quantidade, percentual(quantidade, total));
                })
                .toList();
        List<ChamadoResumo> antigos = consultas.pendentesMaisAntigos(filtro, PENDENTES_EXIBIDOS).stream()
                .map(ChamadoResumo::de)
                .toList();
        return new SituacaoChamados(total, porStatus, antigos);
    }

    public List<ContagemTipo> tiposMaisAtendidos(FiltroChamados filtro, int limite) {
        Map<TipoChamado, Long> totais = consultas.contarPorTipo(filtro);
        Map<TipoChamado, ChamadoConsultas.Resolucao> resolvidos = consultas.resolvidosPorTipo(filtro);
        long total = totais.values().stream().mapToLong(Long::longValue).sum();
        return totais.entrySet().stream()
                .map(item -> {
                    ChamadoConsultas.Resolucao resolucao = resolvidos.get(item.getKey());
                    return new ContagemTipo(item.getKey(), item.getKey().getDescricao(), item.getValue(),
                            resolucao == null ? 0 : resolucao.quantidade(),
                            resolucao == null ? null : resolucao.tempoMedioHoras(),
                            percentual(item.getValue(), total));
                })
                .sorted(Comparator.comparingLong(ContagemTipo::total).reversed())
                .limit(limite)
                .toList();
    }

    public SeriePeriodo atendimentosPorPeriodo(FiltroChamados filtro, Agrupamento agrupamento) {
        LocalDate fim = filtro.fim() != null ? filtro.fim() : LocalDate.now(FiltroChamados.FUSO);
        LocalDate inicio = filtro.inicio() != null ? filtro.inicio() : fim.minusDays(29);
        if (ChronoUnit.DAYS.between(inicio, fim) + 1 > agrupamento.getLimiteDias()) {
            throw new FiltroInvalidoException("Período longo demais para o agrupamento " + agrupamento.name()
                    + ": o máximo é " + agrupamento.getLimiteDias() + " dias. Use um agrupamento maior.");
        }
        FiltroChamados periodo = filtro.comPeriodo(inicio, fim);
        Map<String, Long> abertos = consultas.contarAbertosPorPeriodo(periodo, agrupamento.getFormatoMongo());
        Map<String, Long> resolvidos = consultas.contarResolvidosPorPeriodo(periodo, agrupamento.getFormatoMongo());

        List<PontoPeriodo> pontos = new ArrayList<>();
        for (LocalDate data = agrupamento.inicio(inicio); !data.isAfter(fim); data = agrupamento.proximo(data)) {
            String chave = agrupamento.chave(data);
            pontos.add(new PontoPeriodo(chave, agrupamento.rotulo(data),
                    abertos.getOrDefault(chave, 0L), resolvidos.getOrDefault(chave, 0L)));
        }
        long totalAbertos = abertos.values().stream().mapToLong(Long::longValue).sum();
        long totalResolvidos = resolvidos.values().stream().mapToLong(Long::longValue).sum();
        return new SeriePeriodo(agrupamento, inicio, fim, totalAbertos, totalResolvidos, pontos);
    }

    public OpcoesFiltro opcoes() {
        return new OpcoesFiltro(
                Arrays.stream(StatusChamado.values()).map(s -> new Opcao(s.name(), s.getDescricao())).toList(),
                Arrays.stream(TipoChamado.values()).map(t -> new Opcao(t.name(), t.getDescricao())).toList(),
                Arrays.stream(Prioridade.values()).map(p -> new Opcao(p.name(), p.getDescricao())).toList(),
                consultas.tecnicos(),
                Arrays.stream(Agrupamento.values()).map(a -> new Opcao(a.name(), a.getDescricao())).toList(),
                consultas.primeiraAbertura().map(data -> LocalDate.ofInstant(data, FiltroChamados.FUSO)).orElse(null));
    }

    private long contar(List<Chamado> chamados, Predicate<Chamado> condicao) {
        return chamados.stream().filter(condicao).count();
    }

    private Double mediaHoras(Stream<Duration> duracoes) {
        OptionalDouble media = duracoes.mapToLong(Duration::toMinutes).average();
        return media.isPresent() ? arredondar(media.getAsDouble() / 60.0) : null;
    }

    private double percentual(long parte, long todo) {
        return todo == 0 ? 0 : arredondar(parte * 100.0 / todo);
    }

    private double arredondar(double valor) {
        return Math.round(valor * 10) / 10.0;
    }
}