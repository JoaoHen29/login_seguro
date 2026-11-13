package com.joaohen.login_seguro.chamado;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AberturaChamado(
        @NotBlank(message = "Informe o título.")
        @Size(max = 120, message = "O título deve ter no máximo 120 caracteres.")
        String titulo,

        @NotBlank(message = "Descreva o problema.")
        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres.")
        String descricao,

        @NotNull(message = "Informe o tipo do chamado.")
        TipoChamado tipo,

        @NotNull(message = "Informe a prioridade.")
        Prioridade prioridade,

        @NotBlank(message = "Informe o solicitante.")
        @Size(max = 80, message = "O solicitante deve ter no máximo 80 caracteres.")
        String solicitante,

        @NotBlank(message = "Informe o setor.")
        @Size(max = 60, message = "O setor deve ter no máximo 60 caracteres.")
        String setor) {
}