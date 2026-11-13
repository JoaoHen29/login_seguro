package com.joaohen.login_seguro.chamado;

public enum Prioridade {

    BAIXA("Baixa", 72),
    MEDIA("Média", 24),
    ALTA("Alta", 8),
    CRITICA("Crítica", 4);

    private final String descricao;
    private final int prazoHoras;

    Prioridade(String descricao, int prazoHoras) {
        this.descricao = descricao;
        this.prazoHoras = prazoHoras;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getPrazoHoras() {
        return prazoHoras;
    }
}