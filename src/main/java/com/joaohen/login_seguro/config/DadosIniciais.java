package com.joaohen.login_seguro.config;

import com.joaohen.login_seguro.usuario.Perfil;
import com.joaohen.login_seguro.usuario.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DadosIniciais implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosIniciais.class);

    private final UsuarioService usuarioService;
    private final ContaInicialProperties contas;

    public DadosIniciais(UsuarioService usuarioService, ContaInicialProperties contas) {
        this.usuarioService = usuarioService;
        this.contas = contas;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!contas.criar()) {
            return;
        }
        if (contas.senha() == null || contas.senha().isBlank()) {
            log.warn("Contas iniciais não criadas: defina a variável SENHA_INICIAL.");
            return;
        }
        criarSeNaoExistir("Administrador do Sistema", contas.emailAdmin(), Perfil.ADMIN);
        criarSeNaoExistir("Moderador do Sistema", contas.emailModerador(), Perfil.MODERADOR);
        criarSeNaoExistir("Usuario de Teste", contas.emailUsuario(), Perfil.USUARIO);
    }

    private void criarSeNaoExistir(String nome, String email, Perfil perfil) {
        if (!usuarioService.existeEmail(email)) {
            usuarioService.criar(nome, email, contas.senha(), perfil);
            log.info("Conta inicial criada: {} ({})", email, perfil);
        }
    }
}
