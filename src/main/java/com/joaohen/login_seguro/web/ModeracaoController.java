package com.joaohen.login_seguro.web;

import com.joaohen.login_seguro.seguranca.RegistroAcessoService;
import com.joaohen.login_seguro.usuario.Perfil;
import com.joaohen.login_seguro.usuario.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.EnumMap;
import java.util.Map;

@Controller
@RequestMapping("/moderacao")
public class ModeracaoController {

    private final UsuarioService usuarioService;
    private final RegistroAcessoService registroAcessoService;

    public ModeracaoController(UsuarioService usuarioService, RegistroAcessoService registroAcessoService) {
        this.usuarioService = usuarioService;
        this.registroAcessoService = registroAcessoService;
    }

    @GetMapping
    public String visaoGeral(Model model) {
        Map<Perfil, Long> totais = new EnumMap<>(Perfil.class);
        for (Perfil perfil : Perfil.values()) {
            totais.put(perfil, usuarioService.contarPorPerfil(perfil));
        }
        model.addAttribute("totais", totais);
        model.addAttribute("usuarios", usuarioService.listarTodos());
        model.addAttribute("acessos", registroAcessoService.ultimosDoSistema());
        return "moderacao/moderacao";
    }
}
