package com.joaohen.login_seguro.chamado;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AtualizacaoStatus(
        @NotNull(message = "Informe o novo status.")
        StatusChamado status,

        @NotBlank(message = "Registre a consideração técnica da mudança.")
        @Size(max = 1000, message = "A consideração deve ter no máximo 1000 caracteres.")
        String consideracao,

        @Size(max = 80, message = "O nome do técnico deve ter no máximo 80 caracteres.")
        String tecnicoResponsavel) {
}