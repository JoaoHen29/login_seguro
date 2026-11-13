package com.joaohen.login_seguro.chamado;

import com.joaohen.login_seguro.comum.RegraDeNegocioException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;

@Service
public class ChamadoService {

    private final ChamadoRepository repositorio;
    private final ChamadoConsultas consultas;

    public ChamadoService(ChamadoRepository repositorio, ChamadoConsultas consultas) {
        this.repositorio = repositorio;
        this.consultas = consultas;
    }

    public Page<Chamado> listar(FiltroChamados filtro, Pageable paginacao) {
        return consultas.buscar(filtro, paginacao);
    }

    public Chamado buscar(String protocolo) {
        String normalizado = protocolo.trim().toUpperCase(Locale.ROOT);
        return repositorio.findByProtocolo(normalizado)
                .orElseThrow(() -> new ChamadoNaoEncontradoException(normalizado));
    }

    public Chamado abrir(AberturaChamado dados, String autor) {
        Chamado chamado = new Chamado();
        chamado.setTitulo(dados.titulo().trim());
        chamado.setDescricao(dados.descricao().trim());
        chamado.setTipo(dados.tipo());
        chamado.setPrioridade(dados.prioridade());
        chamado.setSolicitante(dados.solicitante().trim());
        chamado.setSetor(dados.setor().trim());
        chamado.setAbertoEm(Instant.now());
        chamado.setProtocolo(proximoProtocolo());
        chamado.registrar(new Acompanhamento(autor, null, StatusChamado.ABERTO, "Chamado registrado."));
        return repositorio.save(chamado);
    }

    public Chamado atualizarStatus(String protocolo, AtualizacaoStatus dados, String autor) {
        Chamado chamado = buscar(protocolo);
        StatusChamado atual = chamado.getStatus();
        StatusChamado novo = dados.status();

        if (atual.isEncerrado()) {
            throw new RegraDeNegocioException("O chamado " + chamado.getProtocolo() + " já está "
                    + atual.getDescricao().toLowerCase(Locale.ROOT) + " e não pode mais ser alterado.");
        }
        if (novo == atual) {
            throw new RegraDeNegocioException("O chamado já está com o status " + atual.getDescricao() + ".");
        }
        if (novo == StatusChamado.ABERTO) {
            throw new RegraDeNegocioException("Um chamado em andamento não pode voltar para Aberto.");
        }

        String tecnico = dados.tecnicoResponsavel() != null && !dados.tecnicoResponsavel().isBlank()
                ? dados.tecnicoResponsavel().trim()
                : chamado.getTecnicoResponsavel();
        if (tecnico == null && novo != StatusChamado.CANCELADO) {
            throw new RegraDeNegocioException("Informe o técnico responsável para iniciar o atendimento.");
        }

        Instant agora = Instant.now();
        chamado.setTecnicoResponsavel(tecnico);
        if (chamado.getInicioAtendimentoEm() == null && novo != StatusChamado.CANCELADO) {
            chamado.setInicioAtendimentoEm(agora);
        }
        if (novo.isEncerrado()) {
            chamado.setEncerradoEm(agora);
        }
        chamado.setStatus(novo);
        chamado.registrar(new Acompanhamento(autor, atual, novo, dados.consideracao().trim()));
        return repositorio.save(chamado);
    }

    private String proximoProtocolo() {
        int ultimo = repositorio.findTopByOrderByProtocoloDesc()
                .map(chamado -> Integer.parseInt(chamado.getProtocolo().substring(3)))
                .orElse(0);
        return String.format("TD-%06d", ultimo + 1);
    }
}