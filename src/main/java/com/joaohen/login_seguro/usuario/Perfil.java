package com.joaohen.login_seguro.usuario;

public enum Perfil {

    ADMIN("Administrador", "Gerencia usuários e perfis"),
    MODERADOR("Moderador", "Acompanha usuários e acessos"),
    USUARIO("Usuário", "Acesso à área pessoal");

    private final String nome;
    private final String descricao;

    Perfil(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getCodigo() {
        return name().substring(0, 3);
    }
}
