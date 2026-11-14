package com.joaohen.login_seguro.api;

import com.joaohen.login_seguro.chamado.FiltroChamados;
import com.joaohen.login_seguro.chamado.Prioridade;
import com.joaohen.login_seguro.chamado.StatusChamado;
import com.joaohen.login_seguro.chamado.TipoChamado;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class ParametrosFiltro {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate inicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fim;

    private StatusChamado status;
    private TipoChamado tipo;
    private Prioridade prioridade;
    private String tecnico;

    public FiltroChamados paraFiltro() {
        return new FiltroChamados(inicio, fim, status, tipo, prioridade, tecnico);
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public void setInicio(LocalDate inicio) {
        this.inicio = inicio;
    }

    public LocalDate getFim() {
        return fim;
    }

    public void setFim(LocalDate fim) {
        this.fim = fim;
    }

    public StatusChamado getStatus() {
        return status;
    }

    public void setStatus(StatusChamado status) {
        this.status = status;
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

    public String getTecnico() {
        return tecnico;
    }

    public void setTecnico(String tecnico) {
        this.tecnico = tecnico;
    }
}