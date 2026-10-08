package com.joaohen.login_seguro.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({AparenciaProperties.class, ContaInicialProperties.class})
public class AplicacaoConfig {
}
