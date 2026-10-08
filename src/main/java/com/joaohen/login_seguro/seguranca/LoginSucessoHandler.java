package com.joaohen.login_seguro.seguranca;

import com.joaohen.login_seguro.usuario.UsuarioService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginSucessoHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UsuarioService usuarioService;
    private final RegistroAcessoService registroAcessoService;

    public LoginSucessoHandler(UsuarioService usuarioService, RegistroAcessoService registroAcessoService) {
        this.usuarioService = usuarioService;
        this.registroAcessoService = registroAcessoService;
        setDefaultTargetUrl("/painel");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest requisicao, HttpServletResponse resposta,
                                        Authentication autenticacao) throws ServletException, IOException {
        usuarioService.registrarAcessoComSucesso(autenticacao.getName());
        registroAcessoService.registrar(autenticacao.getName(), TipoEvento.LOGIN, requisicao);
        super.onAuthenticationSuccess(requisicao, resposta, autenticacao);
    }
}
