package com.joaohen.login_seguro.chamado;

import java.time.Instant;

public class Acompanhamento {

    private Instant data;
    private String autor;
    private StatusChamado statusAnterior;
    private StatusChamado statusNovo;
    private String consideracao;

    public Acompanhamento() {
    }

    public Acompanhamento(String autor, StatusChamado statusAnterior, StatusChamado statusNovo, String consideracao) {
        this.data = Instant.now();
        this.autor = autor;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.consideracao = consideracao;
    }

    public Instant getData() {
        return data;
    }

    public void setData(Instant data) {
        this.data = data;
    }

    public String getAutor() {
        return autor;
    }

    public String getConsideracao() {
        return consideracao;
    }

    public StatusChamado getStatusAnterior() {
        return statusAnterior;
    }

    public StatusChamado getStatusNovo() {
        return statusNovo;
    }
}