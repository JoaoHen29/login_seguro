package com.joaohen.login_seguro.web;

import com.joaohen.login_seguro.config.AparenciaProperties;
import com.joaohen.login_seguro.usuario.Usuario;
import com.joaohen.login_seguro.usuario.UsuarioService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class AtributosGlobais {

    private final AparenciaProperties aparencia;
    private final UsuarioService usuarioService;

    public AtributosGlobais(AparenciaProperties aparencia, UsuarioService usuarioService) {
        this.aparencia = aparencia;
        this.usuarioService = usuarioService;
    }

    @ModelAttribute("aparencia")
    public AparenciaProperties aparencia() {
        return aparencia;
    }

    @ModelAttribute("tema")
    public String tema() {
        return aparencia.tema();
    }

    @ModelAttribute("usuarioLogado")
    public Usuario usuarioLogado(Authentication autenticacao) {
        if (autenticacao == null || autenticacao instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return usuarioService.buscarPorEmail(autenticacao.getName()).orElse(null);
    }
}
