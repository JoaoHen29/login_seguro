package com.joaohen.login_seguro.api;

import java.time.Instant;
import java.util.Map;

public record ErroApi(Instant dataHora, int status, String erro, String mensagem,
                      String caminho, Map<String, String> campos) {
}