package com.joaohen.login_seguro.seguranca;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegistroAcessoService {

    private final RegistroAcessoRepository repositorio;

    public RegistroAcessoService(RegistroAcessoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void registrar(String email, TipoEvento evento, HttpServletRequest requisicao) {
        repositorio.save(RegistroAcesso.de(email, evento, ipDe(requisicao), navegadorDe(requisicao)));
    }

    public List<RegistroAcesso> ultimosDoUsuario(String email) {
        return repositorio.findTop8ByEmailOrderByDataDesc(email);
    }

    public List<RegistroAcesso> ultimosDoSistema() {
        return repositorio.findTop30ByOrderByDataDesc();
    }

    private String ipDe(HttpServletRequest requisicao) {
        String encaminhado = requisicao.getHeader("X-Forwarded-For");
        if (encaminhado != null && !encaminhado.isBlank()) {
            return encaminhado.split(",")[0].trim();
        }
        return requisicao.getRemoteAddr();
    }

    private String navegadorDe(HttpServletRequest requisicao) {
        String agente = requisicao.getHeader("User-Agent");
        if (agente == null) {
            return "Desconhecido";
        }
        if (agente.contains("Edg/")) {
            return "Edge";
        }
        if (agente.contains("OPR/")) {
            return "Opera";
        }
        if (agente.contains("Firefox/")) {
            return "Firefox";
        }
        if (agente.contains("Chrome/")) {
            return "Chrome";
        }
        if (agente.contains("Safari/")) {
            return "Safari";
        }
        if (agente.contains("PostmanRuntime")) {
            return "Postman";
        }
        return "Outro";
    }
}
