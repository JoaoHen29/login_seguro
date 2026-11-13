package com.joaohen.login_seguro.api;

import com.joaohen.login_seguro.chamado.Chamado;
import com.joaohen.login_seguro.chamado.ChamadoResumo;
import org.springframework.data.domain.Page;

import java.util.List;

public record PaginaChamados(List<ChamadoResumo> itens, int pagina, int tamanho,
                             long totalItens, int totalPaginas) {

    public static PaginaChamados de(Page<Chamado> pagina) {
        return new PaginaChamados(
                pagina.getContent().stream().map(ChamadoResumo::de).toList(),
                pagina.getNumber(), pagina.getSize(), pagina.getTotalElements(), pagina.getTotalPages());
    }
}