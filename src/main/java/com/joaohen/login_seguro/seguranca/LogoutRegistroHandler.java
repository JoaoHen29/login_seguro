package com.joaohen.login_seguro.seguranca;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
public class LogoutRegistroHandler implements LogoutHandler {

    private final RegistroAcessoService registroAcessoService;

    public LogoutRegistroHandler(RegistroAcessoService registroAcessoService) {
        this.registroAcessoService = registroAcessoService;
    }

    @Override
    public void logout(HttpServletRequest requisicao, HttpServletResponse resposta, Authentication autenticacao) {
        if (autenticacao != null) {
            registroAcessoService.registrar(autenticacao.getName(), TipoEvento.LOGOUT, requisicao);
        }
    }
}
