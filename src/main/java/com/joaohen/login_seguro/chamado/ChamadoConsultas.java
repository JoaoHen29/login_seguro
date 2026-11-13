package com.joaohen.login_seguro.chamado;

import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public class ChamadoConsultas {

    private static final String COLECAO = "chamados";

    private static final List<String> PENDENTES = Arrays.stream(StatusChamado.values())
            .filter(status -> !status.isEncerrado())
            .map(Enum::name)
            .toList();

    public record Resolucao(long quantidade, Double tempoMedioHoras) {
    }

    private final MongoTemplate mongo;

    public ChamadoConsultas(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    public Page<Chamado> buscar(FiltroChamados filtro, Pageable paginacao) {
        Query consulta = consulta(condicoes(filtro, "abertoEm"));
        long total = mongo.count(consulta, Chamado.class);
        List<Chamado> chamados = mongo.find(consulta.with(paginacao), Chamado.class);
        return new PageImpl<>(chamados, paginacao, total);
    }

    public List<Chamado> listarParaIndicadores(FiltroChamados filtro) {
        Query consulta = consulta(condicoes(filtro, "abertoEm"));
        consulta.fields().include("status", "prioridade", "abertoEm", "inicioAtendimentoEm", "encerradoEm");
        return mongo.find(consulta, Chamado.class);
    }

    public List<Chamado> pendentesMaisAntigos(FiltroChamados filtro, int limite) {
        List<Criteria> condicoes = condicoes(filtro, "abertoEm");
        condicoes.add(Criteria.where("status").in(PENDENTES));
        Query consulta = consulta(condicoes).with(Sort.by(Sort.Direction.ASC, "abertoEm")).limit(limite);
        return mongo.find(consulta, Chamado.class);
    }

    public Map<StatusChamado, Long> contarPorStatus(FiltroChamados filtro) {
        Map<StatusChamado, Long> totais = new EnumMap<>(StatusChamado.class);
        for (Document linha : agrupar(condicoes(filtro, "abertoEm"),
                Aggregation.group("status").count().as("total"))) {
            totais.put(StatusChamado.valueOf(linha.getString("_id")), numero(linha, "total"));
        }
        return totais;
    }

    public Map<TipoChamado, Long> contarPorTipo(FiltroChamados filtro) {
        Map<TipoChamado, Long> totais = new EnumMap<>(TipoChamado.class);
        for (Document linha : agrupar(condicoes(filtro, "abertoEm"),
                Aggregation.group("tipo").count().as("total"))) {
            totais.put(TipoChamado.valueOf(linha.getString("_id")), numero(linha, "total"));
        }
        return totais;
    }

    public Map<TipoChamado, Resolucao> resolvidosPorTipo(FiltroChamados filtro) {
        List<Criteria> condicoes = condicoes(filtro, "abertoEm");
        condicoes.add(Criteria.where("status").is(StatusChamado.RESOLVIDO.name()));
        Map<TipoChamado, Resolucao> resultado = new EnumMap<>(TipoChamado.class);
        for (Document linha : agrupar(condicoes, Aggregation.group("tipo").count().as("total")
                .avg(ArithmeticOperators.Subtract.valueOf("encerradoEm").subtract("abertoEm")).as("tempoMedioMs"))) {
            Number media = linha.get("tempoMedioMs", Number.class);
            Double horas = media == null ? null : Math.round(media.doubleValue() / 360_000.0) / 10.0;
            resultado.put(TipoChamado.valueOf(linha.getString("_id")), new Resolucao(numero(linha, "total"), horas));
        }
        return resultado;
    }

    public Map<String, Long> contarAbertosPorPeriodo(FiltroChamados filtro, String formato) {
        return contarPorPeriodo(condicoes(filtro, "abertoEm"), "abertoEm", formato);
    }

    public Map<String, Long> contarResolvidosPorPeriodo(FiltroChamados filtro, String formato) {
        List<Criteria> condicoes = condicoes(filtro, "encerradoEm");
        condicoes.add(Criteria.where("status").is(StatusChamado.RESOLVIDO.name()));
        return contarPorPeriodo(condicoes, "encerradoEm", formato);
    }

    public List<String> tecnicos() {
        return mongo.findDistinct(new Query(), "tecnicoResponsavel", COLECAO, String.class).stream()
                .filter(Objects::nonNull)
                .sorted()
                .toList();
    }

    public Optional<Instant> primeiraAbertura() {
        Query consulta = new Query().with(Sort.by(Sort.Direction.ASC, "abertoEm")).limit(1);
        consulta.fields().include("abertoEm");
        return Optional.ofNullable(mongo.findOne(consulta, Chamado.class)).map(Chamado::getAbertoEm);
    }

    private List<Criteria> condicoes(FiltroChamados filtro, String campoData) {
        List<Criteria> condicoes = new ArrayList<>();
        Instant inicio = filtro.inicioDoPeriodo();
        Instant fim = filtro.fimDoPeriodo();
        if (inicio != null) {
            condicoes.add(Criteria.where(campoData).gte(Date.from(inicio)));
        }
        if (fim != null) {
            condicoes.add(Criteria.where(campoData).lt(Date.from(fim)));
        }
        if (filtro.status() != null) {
            condicoes.add(Criteria.where("status").is(filtro.status().name()));
        }
        if (filtro.tipo() != null) {
            condicoes.add(Criteria.where("tipo").is(filtro.tipo().name()));
        }
        if (filtro.prioridade() != null) {
            condicoes.add(Criteria.where("prioridade").is(filtro.prioridade().name()));
        }
        if (filtro.tecnico() != null) {
            condicoes.add(Criteria.where("tecnicoResponsavel").is(filtro.tecnico()));
        }
        return condicoes;
    }

    private Map<String, Long> contarPorPeriodo(List<Criteria> condicoes, String campoData, String formato) {
        Map<String, Long> totais = new HashMap<>();
        AggregationOperation periodo = Aggregation.project()
                .and(DateOperators.dateOf(campoData)
                        .withTimezone(DateOperators.Timezone.valueOf(FiltroChamados.FUSO.getId()))
                        .toString(formato))
                .as("periodo");
        for (Document linha : agrupar(condicoes, periodo, Aggregation.group("periodo").count().as("total"))) {
            totais.put(linha.getString("_id"), numero(linha, "total"));
        }
        return totais;
    }

    private List<Document> agrupar(List<Criteria> condicoes, AggregationOperation... etapas) {
        List<AggregationOperation> pipeline = new ArrayList<>();
        pipeline.add(Aggregation.match(juntar(condicoes)));
        pipeline.addAll(Arrays.asList(etapas));
        return mongo.aggregate(Aggregation.newAggregation(pipeline), COLECAO, Document.class).getMappedResults();
    }

    private Query consulta(List<Criteria> condicoes) {
        return new Query(juntar(condicoes));
    }

    private Criteria juntar(List<Criteria> condicoes) {
        return condicoes.isEmpty()
                ? Criteria.where("_id").exists(true)
                : new Criteria().andOperator(condicoes);
    }

    private long numero(Document linha, String campo) {
        Number valor = linha.get(campo, Number.class);
        return valor == null ? 0 : valor.longValue();
    }
}