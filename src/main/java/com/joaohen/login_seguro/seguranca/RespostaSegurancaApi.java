package com.joaohen.login_seguro.seguranca;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class RespostaSegurancaApi implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest requisicao, HttpServletResponse resposta,
                         AuthenticationException excecao) throws IOException {
        escrever(requisicao, resposta, 401, "Unauthorized",
                "Autenticação necessária. Envie e-mail e senha de um usuário ADMIN ou MODERADOR.");
    }

    @Override
    public void handle(HttpServletRequest requisicao, HttpServletResponse resposta,
                       AccessDeniedException excecao) throws IOException {
        escrever(requisicao, resposta, 403, "Forbidden",
                "Seu perfil não tem acesso à API. Ela é restrita a ADMIN e MODERADOR.");
    }

    private void escrever(HttpServletRequest requisicao, HttpServletResponse resposta,
                          int status, String erro, String mensagem) throws IOException {
        resposta.setStatus(status);
        resposta.setContentType("application/json");
        resposta.setCharacterEncoding("UTF-8");
        resposta.getWriter().write(String.format(
                "{\"dataHora\":\"%s\",\"status\":%d,\"erro\":\"%s\",\"mensagem\":\"%s\",\"caminho\":\"%s\",\"campos\":{}}",
                Instant.now(), status, erro, escapar(mensagem), escapar(requisicao.getRequestURI())));
    }

    private String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}