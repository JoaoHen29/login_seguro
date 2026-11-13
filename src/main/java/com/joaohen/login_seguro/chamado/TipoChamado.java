package com.joaohen.login_seguro.chamado;

public enum TipoChamado {

    HARDWARE("Hardware"),
    SOFTWARE("Software"),
    REDE("Rede e internet"),
    ACESSO("Acesso e senha"),
    IMPRESSORA("Impressora"),
    EMAIL("E-mail"),
    OUTROS("Outros");

    private final String descricao;

    TipoChamado(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
