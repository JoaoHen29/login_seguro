package com.joaohen.login_seguro.seguranca;

public enum TipoEvento {

    LOGIN("Entrada"),
    LOGOUT("Saída"),
    FALHA("Senha recusada"),
    BLOQUEIO("Conta bloqueada");

    private final String descricao;

    TipoEvento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
