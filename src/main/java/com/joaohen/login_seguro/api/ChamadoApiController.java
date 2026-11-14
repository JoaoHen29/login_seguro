package com.joaohen.login_seguro.api;

import com.joaohen.login_seguro.chamado.AberturaChamado;
import com.joaohen.login_seguro.chamado.AtualizacaoStatus;
import com.joaohen.login_seguro.chamado.Chamado;
import com.joaohen.login_seguro.chamado.ChamadoDetalhe;
import com.joaohen.login_seguro.chamado.ChamadoService;
import com.joaohen.login_seguro.chamado.FiltroInvalidoException;
import com.joaohen.login_seguro.usuario.Usuario;
import com.joaohen.login_seguro.usuario.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/chamados")
public class ChamadoApiController {

    private static final int TAMANHO_MAXIMO = 100;

    private final ChamadoService chamados;
    private final UsuarioService usuarios;

    public ChamadoApiController(ChamadoService chamados, UsuarioService usuarios) {
        this.chamados = chamados;
        this.usuarios = usuarios;
    }

    @GetMapping
    public PaginaChamados listar(@ModelAttribute ParametrosFiltro parametros,
                                 @RequestParam(name = "pagina", defaultValue = "0") int pagina,
                                 @RequestParam(name = "tamanho", defaultValue = "20") int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > TAMANHO_MAXIMO) {
            throw new FiltroInvalidoException("Use pagina a partir de 0 e tamanho entre 1 e " + TAMANHO_MAXIMO + ".");
        }
        Page<Chamado> resultado = chamados.listar(parametros.paraFiltro(),
                PageRequest.of(pagina, tamanho, Sort.by(Sort.Direction.DESC, "abertoEm")));
        return PaginaChamados.de(resultado);
    }

    @GetMapping("/{protocolo}")
    public ChamadoDetalhe detalhar(@PathVariable("protocolo") String protocolo) {
        return ChamadoDetalhe.de(chamados.buscar(protocolo));
    }

    @PostMapping
    public ResponseEntity<ChamadoDetalhe> abrir(@Valid @RequestBody AberturaChamado dados,
                                                Authentication autenticacao) {
        Chamado chamado = chamados.abrir(dados, autor(autenticacao));
        return ResponseEntity.created(URI.create("/api/chamados/" + chamado.getProtocolo()))
                .body(ChamadoDetalhe.de(chamado));
    }

    @PatchMapping("/{protocolo}/status")
    public ChamadoDetalhe atualizarStatus(@PathVariable("protocolo") String protocolo,
                                          @Valid @RequestBody AtualizacaoStatus dados,
                                          Authentication autenticacao) {
        return ChamadoDetalhe.de(chamados.atualizarStatus(protocolo, dados, autor(autenticacao)));
    }

    private String autor(Authentication autenticacao) {
        return usuarios.buscarPorEmail(autenticacao.getName())
                .map(Usuario::getNome)
                .orElse(autenticacao.getName());
    }
}