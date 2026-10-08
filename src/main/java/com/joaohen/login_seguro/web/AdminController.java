package com.joaohen.login_seguro.web;

import com.joaohen.login_seguro.comum.RegraDeNegocioException;
import com.joaohen.login_seguro.usuario.Perfil;
import com.joaohen.login_seguro.usuario.Usuario;
import com.joaohen.login_seguro.usuario.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
public class AdminController {

    private final UsuarioService usuarioService;

    public AdminController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        model.addAttribute("perfis", Perfil.values());
        return "admin/usuarios";
    }

    @PostMapping("/{id}/perfil")
    public String alterarPerfil(@PathVariable("id") String id, @RequestParam("perfil") Perfil perfil,
                                Authentication autenticacao, RedirectAttributes redirecionamento) {
        try {
            usuarioService.alterarPerfil(id, perfil, autenticacao.getName());
            redirecionamento.addFlashAttribute("sucesso", "Perfil alterado para " + perfil.getNome() + ".");
        } catch (RegraDeNegocioException e) {
            redirecionamento.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/{id}/status")
    public String alternarStatus(@PathVariable("id") String id, Authentication autenticacao,
                                 RedirectAttributes redirecionamento) {
        try {
            Usuario usuario = usuarioService.alternarAtivo(id, autenticacao.getName());
            String situacao = usuario.isAtivo() ? "reativada" : "desativada";
            redirecionamento.addFlashAttribute("sucesso", "Conta de " + usuario.getNome() + " " + situacao + ".");
        } catch (RegraDeNegocioException e) {
            redirecionamento.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }
}
