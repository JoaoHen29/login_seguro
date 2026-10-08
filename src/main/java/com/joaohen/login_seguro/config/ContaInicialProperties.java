package com.joaohen.login_seguro.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.contas-iniciais")
public record ContaInicialProperties(
        @DefaultValue("true") boolean criar,
        @DefaultValue("admin@loginseguro.dev") String emailAdmin,
        @DefaultValue("moderador@loginseguro.dev") String emailModerador,
        @DefaultValue("usuario@loginseguro.dev") String emailUsuario,
        String senha) {
}
