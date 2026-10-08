package com.joaohen.login_seguro.seguranca;

import com.joaohen.login_seguro.usuario.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginFalhaHandler implements AuthenticationFailureHandler {

    private final UsuarioService usuarioService;
    private final RegistroAcessoService registroAcessoService;

    public LoginFalhaHandler(UsuarioService usuarioService, RegistroAcessoService registroAcessoService) {
        this.usuarioService = usuarioService;
        this.registroAcessoService = registroAcessoService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest requisicao, HttpServletResponse resposta,
                                        AuthenticationException excecao) throws IOException {
        String email = UsuarioService.normalizarEmail(requisicao.getParameter("email"));
        String destino = destinoPara(excecao, email, requisicao);
        resposta.sendRedirect(requisicao.getContextPath() + destino);
    }

    private String destinoPara(AuthenticationException excecao, String email, HttpServletRequest requisicao) {
        if (excecao instanceof LockedException) {
            return "/login?bloqueado";
        }
        if (excecao instanceof DisabledException) {
            return "/login?inativo";
        }
        if (email.isEmpty()) {
            return "/login?erro";
        }
        registroAcessoService.registrar(email, TipoEvento.FALHA, requisicao);
        if (usuarioService.registrarFalhaDeAcesso(email)) {
            registroAcessoService.registrar(email, TipoEvento.BLOQUEIO, requisicao);
            return "/login?bloqueado";
        }
        return "/login?erro";
    }
}
