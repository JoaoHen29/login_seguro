package com.joaohen.login_seguro.web;

import com.joaohen.login_seguro.seguranca.RegistroAcessoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PainelController {

    private final RegistroAcessoService registroAcessoService;

    public PainelController(RegistroAcessoService registroAcessoService) {
        this.registroAcessoService = registroAcessoService;
    }

    @GetMapping("/")
    public String inicio() {
        return "redirect:/painel";
    }

    @GetMapping("/painel")
    public String painel(Authentication autenticacao, Model model) {
        model.addAttribute("acessos", registroAcessoService.ultimosDoUsuario(autenticacao.getName()));
        return "painel/painel";
    }

    @GetMapping("/acesso-negado")
    public String acessoNegado() {
        return "erro/acesso-negado";
    }
}
