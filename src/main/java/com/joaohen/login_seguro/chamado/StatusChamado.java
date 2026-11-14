package com.joaohen.login_seguro.chamado;

public enum StatusChamado {

    ABERTO("Aberto", false),
    EM_ATENDIMENTO("Em atendimento", false),
    AGUARDANDO_USUARIO("Aguardando usuário", false),
    RESOLVIDO("Resolvido", true),
    CANCELADO("Cancelado", true);

    private final String descricao;
    private final boolean encerrado;

    StatusChamado(String descricao, boolean encerrado) {
        this.descricao = descricao;
        this.encerrado = encerrado;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isEncerrado() {
        return encerrado;
    }
}