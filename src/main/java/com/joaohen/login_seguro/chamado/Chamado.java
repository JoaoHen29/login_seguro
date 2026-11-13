package com.joaohen.login_seguro.chamado;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document("chamados")
public class Chamado {

    @Id
    private String id;

    @Indexed(unique = true)
    private String protocolo;

    private String titulo;

    private String descricao;

    @Indexed
    private TipoChamado tipo;

    @Indexed
    private Prioridade prioridade;

    @Indexed
    private StatusChamado status = StatusChamado.ABERTO;

    private String solicitante;

    private String setor;

    @Indexed
    private String tecnicoResponsavel;

    @Indexed
    private Instant abertoEm;

    private Instant inicioAtendimentoEm;

    @Indexed
    private Instant encerradoEm;

    private Instant atualizadoEm;

    private List<Acompanhamento> historico = new ArrayList<>();

    public boolean isPrazoEstourado() {
        Instant referencia = encerradoEm != null ? encerradoEm : Instant.now();
        return Duration.between(abertoEm, referencia).toHours() > prioridade.getPrazoHoras();
    }

    public void registrar(Acompanhamento acompanhamento) {
        historico.add(acompanhamento);
        atualizadoEm = acompanhamento.getData();
    }

    public String getId() {
        return id;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public TipoChamado getTipo() {
        return tipo;
    }

    public void setTipo(TipoChamado tipo) {
        this.tipo = tipo;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(Prioridade prioridade) {
        this.prioridade = prioridade;
    }

    public StatusChamado getStatus() {
        return status;
    }

    public void setStatus(StatusChamado status) {
        this.status = status;
    }

    public String getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(String solicitante) {
        this.solicitante = solicitante;
    }

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }

    public String getTecnicoResponsavel() {
        return tecnicoResponsavel;
    }

    public void setTecnicoResponsavel(String tecnicoResponsavel) {
        this.tecnicoResponsavel = tecnicoResponsavel;
    }

    public Instant getAbertoEm() {
        return abertoEm;
    }

    public void setAbertoEm(Instant abertoEm) {
        this.abertoEm = abertoEm;
    }

    public Instant getInicioAtendimentoEm() {
        return inicioAtendimentoEm;
    }

    public void setInicioAtendimentoEm(Instant inicioAtendimentoEm) {
        this.inicioAtendimentoEm = inicioAtendimentoEm;
    }

    public Instant getEncerradoEm() {
        return encerradoEm;
    }

    public void setEncerradoEm(Instant encerradoEm) {
        this.encerradoEm = encerradoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(Instant atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public List<Acompanhamento> getHistorico() {
        return historico;
    }
}