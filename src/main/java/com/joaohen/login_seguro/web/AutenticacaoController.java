package com.joaohen.login_seguro.web;

import com.joaohen.login_seguro.usuario.CadastroForm;
import com.joaohen.login_seguro.usuario.EmailJaCadastradoException;
import com.joaohen.login_seguro.usuario.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AutenticacaoController {

    private final UsuarioService usuarioService;

    public AutenticacaoController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String login(Authentication autenticacao) {
        return estaLogado(autenticacao) ? "redirect:/painel" : "auth/login";
    }

    @GetMapping("/cadastro")
    public String formularioCadastro(Authentication autenticacao, Model model) {
        if (estaLogado(autenticacao)) {
            return "redirect:/painel";
        }
        model.addAttribute("form", new CadastroForm());
        return "auth/cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@Valid @ModelAttribute("form") CadastroForm form, BindingResult erros) {
        if (!erros.hasFieldErrors("confirmacaoSenha") && !form.senhasConferem()) {
            erros.rejectValue("confirmacaoSenha", "senha.diferente", "As senhas não são iguais.");
        }
        if (erros.hasErrors()) {
            return "auth/cadastro";
        }
        try {
            usuarioService.cadastrar(form);
        } catch (EmailJaCadastradoException e) {
            erros.rejectValue("email", "email.duplicado", e.getMessage());
            return "auth/cadastro";
        }
        return "redirect:/login?cadastrado";
    }

    private boolean estaLogado(Authentication autenticacao) {
        return autenticacao != null && !(autenticacao instanceof AnonymousAuthenticationToken)
                && autenticacao.isAuthenticated();
    }
}
