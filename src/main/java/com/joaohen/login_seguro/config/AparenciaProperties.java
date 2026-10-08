package com.joaohen.login_seguro.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.aparencia")
public record AparenciaProperties(
        @DefaultValue("Login Seguro") String nomeSistema,
        @DefaultValue("Sua identidade, conferida a cada acesso.") String slogan,
        @DefaultValue("documento") String tema) {
}
